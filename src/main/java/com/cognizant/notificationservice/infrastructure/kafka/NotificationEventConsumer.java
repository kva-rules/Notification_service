package com.cognizant.notificationservice.infrastructure.kafka;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.domain.exception.TemplateNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final NotificationTemplateService templateService;
    private final NotificationEventProducer eventProducer;

    @KafkaListener(topics = "ticket.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTicketCreated(NotificationEvent event) {
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

    @KafkaListener(topics = "solution.approved", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeSolutionApproved(NotificationEvent event) {
        processEvent(event, "SOLUTION_APPROVED");
    }

    @KafkaListener(topics = "knowledge.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeKnowledgeCreated(NotificationEvent event) {
        processEvent(event, "KNOWLEDGE_CREATED");
    }

    @KafkaListener(topics = "reward.points.added", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRewardPointsAdded(NotificationEvent event) {
        processEvent(event, "REWARD_POINTS_ADDED");
    }

    @KafkaListener(topics = "reward.badge.awarded", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBadgeAwarded(NotificationEvent event) {
        processEvent(event, "BADGE_AWARDED");
    }

    @KafkaListener(topics = "leaderboard.updated", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeLeaderboardUpdated(NotificationEvent event) {
        processEvent(event, "LEADERBOARD_UPDATED");
    }

    private void processEvent(NotificationEvent event, String eventType) {
        log.info("Received {} event", eventType);

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
                message = event.getTemplateVariables() != null ? 
                        event.getTemplateVariables().getOrDefault("message", "You have a new notification") :
                        "You have a new notification";
            }

            CreateNotificationRequest request = CreateNotificationRequest.builder()
                    .title(title)
                    .message(message)
                    .type(NotificationType.valueOf(eventType))
                    .referenceId(event.getReferenceId())
                    .referenceType(event.getReferenceType())
                    .recipientUserIds(event.getRecipientUserIds())
                    .build();

            NotificationResponse response = notificationService.createNotification(request);
            notificationService.sendNotification(response.getNotificationId());

            eventProducer.publishNotificationSent(response.getNotificationId());
            log.info("Successfully processed {} event, notification id: {}", eventType, response.getNotificationId());

        } catch (Exception e) {
            log.error("Error processing {} event: {}", eventType, e.getMessage(), e);
            eventProducer.publishNotificationFailed(event, e.getMessage());
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
