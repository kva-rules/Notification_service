package com.cognizant.notificationservice.infrastructure.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.event.RewardPointsAddedEvent;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.domain.exception.TemplateNotFoundException;
import com.library.common.event.RewardAddedEvent;
import com.library.common.event.SolutionApprovedEvent;
import com.library.common.event.SolutionRejectedEvent;
import com.library.common.event.SolutionSubmittedEvent;
import com.library.common.event.TicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final NotificationEventProducer eventProducer;

    // ── Typed common-library event consumers ──────────────────────────────────

    @KafkaListener(topics = "ticket.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketCreated(TicketCreatedEvent event) {
        log.info("Received ticket.created for ticket: {}", event.getTicketId());
        try {
            if (StringUtils.hasText(event.getTitle()) && event.getAssignedUserId() != null) {
                UUID userUuid = parseUuid(event.getAssignedUserId().toString());
                if (userUuid != null) {
                    createAndSend("New Ticket Assigned",
                            "A new ticket has been assigned to you: " + event.getTitle(),
                            NotificationType.TICKET_ASSIGNED,
                            List.of(userUuid));
                }
            }
        } catch (Exception e) {
            log.error("Error processing ticket.created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "solution.approved", groupId = "${spring.kafka.consumer.group-id}-notify-approved")
    public void consumeSolutionApproved(SolutionApprovedEvent event) {
        log.info("Received solution.approved for solution: {}", event.getSolutionId());
        try {
            List<UUID> recipients = toUuidList(event.getContributorIds());
            if (!recipients.isEmpty()) {
                String title = StringUtils.hasText(event.getSolutionTitle())
                        ? event.getSolutionTitle() : "your solution";
                createAndSend("Solution Approved",
                        "Your solution \"" + title + "\" has been approved and added to the knowledge base.",
                        NotificationType.SOLUTION_APPROVED,
                        recipients);
            }
        } catch (Exception e) {
            log.error("Error processing solution.approved event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "solution.rejected", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSolutionRejected(SolutionRejectedEvent event) {
        log.info("Received solution.rejected for solution: {}", event.getSolutionId());
        try {
            UUID creatorUuid = parseUuid(event.getCreatedBy());
            if (creatorUuid != null) {
                String reason = StringUtils.hasText(event.getRejectionReason())
                        ? " Reason: " + event.getRejectionReason() : "";
                String title = StringUtils.hasText(event.getSolutionTitle())
                        ? event.getSolutionTitle() : "your solution";
                createAndSend("Solution Rejected",
                        "Your solution \"" + title + "\" was rejected." + reason,
                        NotificationType.SOLUTION_APPROVED,
                        List.of(creatorUuid));
            }
        } catch (Exception e) {
            log.error("Error processing solution.rejected event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "solution.submitted", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSolutionSubmitted(SolutionSubmittedEvent event) {
        log.info("Received solution.submitted for solution: {}", event.getSolutionId());
        // No-op for now: admins discover pending solutions via the /solutions/pending endpoint.
        // Extend here to broadcast to all ADMIN/MANAGER users when a user-lookup service is available.
    }

    @KafkaListener(topics = "reward.added", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRewardAdded(RewardAddedEvent event) {
        log.info("Received reward.added for user: {}", event.getUserId());
        try {
            UUID userUuid = parseUuid(event.getUserId());
            if (userUuid != null && event.getPoints() != null) {
                createAndSend("Points Earned",
                        "You earned " + event.getPoints() + " points!",
                        NotificationType.REWARD_POINTS_ADDED,
                        List.of(userUuid));
            }
        } catch (Exception e) {
            log.error("Error processing reward.added event: {}", e.getMessage(), e);
        }
    }

    // ── Legacy NotificationEvent consumers (template-based) ──────────────────

    @KafkaListener(topics = "ticket.created.legacy", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketCreatedLegacy(NotificationEvent event) {
        processEvent(event, "TICKET_CREATED");
    }

    @KafkaListener(topics = "ticket.assigned", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketAssigned(NotificationEvent event) {
        processEvent(event, "TICKET_ASSIGNED");
    }

    @KafkaListener(topics = "ticket.resolved", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketResolved(NotificationEvent event) {
        processEvent(event, "TICKET_RESOLVED");
    }

    @KafkaListener(topics = "knowledge.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKnowledgeCreated(NotificationEvent event) {
        processEvent(event, "KNOWLEDGE_CREATED");
    }

    @KafkaListener(topics = "reward.points.added", groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "rewardPointsContainerFactory")
    public void consumeRewardPointsAdded(RewardPointsAddedEvent event) {
        if (event == null || event.getUserId() == null) return;
        log.info("Received reward.points.added for user: {}", event.getUserId());
        try {
            int pts = event.getPointsAdded() != null ? event.getPointsAdded() : 0;
            createAndSend("Points Earned",
                    "You earned " + pts + " points! Total: " + (event.getTotalPoints() != null ? event.getTotalPoints() : pts),
                    NotificationType.REWARD_POINTS_ADDED,
                    List.of(event.getUserId()));
        } catch (Exception e) {
            log.error("Error processing reward.points.added event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "reward.badge.awarded", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBadgeAwarded(NotificationEvent event) {
        processEvent(event, "BADGE_AWARDED");
    }

    @KafkaListener(topics = "leaderboard.updated", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLeaderboardUpdated(NotificationEvent event) {
        processEvent(event, "LEADERBOARD_UPDATED");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void createAndSend(String title, String message, NotificationType type, List<UUID> recipients) {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title(title)
                .message(message)
                .type(type)
                .recipientUserIds(recipients)
                .build();
        NotificationResponse response = notificationService.createNotification(request);
        notificationService.sendNotification(response.getNotificationId());
    }

    private void processEvent(NotificationEvent event, String eventType) {
        log.info("Processing legacy {} event", eventType);
        try {
            String title;
            String message;

            try {
                NotificationTemplateResponse template = templateService.getTemplateByEventType(eventType);
                title = processTemplate(template.getTitleTemplate(), event.getTemplateVariables());
                message = processTemplate(template.getMessageTemplate(), event.getTemplateVariables());
            } catch (TemplateNotFoundException e) {
                log.warn("Template not found for {}, using default", eventType);
                title = eventType.replace("_", " ");
                message = event.getTemplateVariables() != null
                        ? event.getTemplateVariables().getOrDefault("message", "You have a new notification")
                        : "You have a new notification";
            }

            NotificationType notifType;
            try {
                notifType = NotificationType.valueOf(eventType);
            } catch (IllegalArgumentException e) {
                notifType = NotificationType.SYSTEM_ALERT;
            }

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .message(message)
                    .type(notifType)
                    .referenceId(event.getReferenceId())
                    .referenceType(event.getReferenceType())
                    .recipientUserIds(event.getRecipientUserIds())
                    .build();

            NotificationResponse response = notificationService.createNotification(request);
            notificationService.sendNotification(response.getNotificationId());
            eventProducer.publishNotificationSent(response.getNotificationId());
        } catch (Exception e) {
            log.error("Error processing {} event: {}", eventType, e.getMessage(), e);
            eventProducer.publishNotificationFailed(event, e.getMessage());
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) return template;
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private UUID parseUuid(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse UUID: {}", value);
            return null;
        }
    }

    private List<UUID> toUuidList(List<String> ids) {
        if (ids == null) return Collections.emptyList();
        return ids.stream()
                .map(this::parseUuid)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }
}
