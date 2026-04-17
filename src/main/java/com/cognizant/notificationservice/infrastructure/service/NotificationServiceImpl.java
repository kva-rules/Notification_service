package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.BroadcastNotificationRequest;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationStatisticsResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import com.cognizant.notificationservice.application.mapper.NotificationMapper;
import com.cognizant.notificationservice.application.service.EmailService;
import com.cognizant.notificationservice.application.service.NotificationPreferenceService;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.entity.NotificationActivityLog;
import com.cognizant.notificationservice.domain.entity.NotificationDeliveryLog;
import com.cognizant.notificationservice.domain.entity.NotificationRecipient;
import com.cognizant.notificationservice.domain.enums.DeliveryStatus;
import com.cognizant.notificationservice.domain.enums.DeliveryType;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.domain.exception.NotificationNotFoundException;
import com.cognizant.notificationservice.infrastructure.repository.NotificationRecipientRepository;
import com.cognizant.notificationservice.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationPreferenceService preferenceService;
    private final EmailService emailService;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        log.info("Creating notification with title: {}", request.getTitle());

        Notification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.CREATED);

        List<NotificationRecipient> recipients = request.getRecipientUserIds().stream()
                .map(userId -> NotificationRecipient.builder()
                        .notification(notification)
                        .userId(userId)
                        .read(false)
                        .build())
                .toList();

        notification.setRecipients(recipients);

        NotificationActivityLog activityLog = NotificationActivityLog.builder()
                .notification(notification)
                .action("NOTIFICATION_CREATED")
                .build();
        notification.getActivityLogs().add(activityLog);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with id: {}", savedNotification.getNotificationId());

        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public void sendNotification(UUID notificationId) {
        log.info("Sending notification: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        for (NotificationRecipient recipient : notification.getRecipients()) {
            try {
                boolean shouldSendEmail = checkEmailPreference(recipient.getUserId(), notification);
                
                if (shouldSendEmail) {
                    sendEmailNotification(notification, recipient);
                }

                addDeliveryLog(notification, recipient.getUserId(), DeliveryType.IN_APP, DeliveryStatus.SUCCESS, null);

            } catch (Exception e) {
                log.error("Failed to send notification to user {}: {}", recipient.getUserId(), e.getMessage());
                addDeliveryLog(notification, recipient.getUserId(), DeliveryType.IN_APP, DeliveryStatus.FAILED, e.getMessage());
            }
        }

        notification.setStatus(NotificationStatus.SENT);
        addActivityLog(notification, "NOTIFICATION_SENT");
        notificationRepository.save(notification);

        log.info("Notification sent: {}", notificationId);
    }

    private boolean checkEmailPreference(UUID userId, Notification notification) {
        try {
            var preferences = preferenceService.getPreferencesByUserId(userId);
            if (!preferences.isEmailEnabled()) {
                return false;
            }

            return switch (notification.getType()) {
                case TICKET_CREATED, TICKET_ASSIGNED, TICKET_RESOLVED -> preferences.isTicketUpdates();
                case SOLUTION_CREATED, SOLUTION_APPROVED -> preferences.isSolutionUpdates();
                case KNOWLEDGE_CREATED -> preferences.isKnowledgeUpdates();
                case REWARD_POINTS_ADDED, BADGE_AWARDED, LEADERBOARD_UPDATED -> preferences.isRewardUpdates();
                case SYSTEM_ALERT -> true;
            };
        } catch (Exception e) {
            log.warn("Could not fetch preferences for user {}, defaulting to no email", userId);
            return false;
        }
    }

    private void sendEmailNotification(Notification notification, NotificationRecipient recipient) {
        try {
            emailService.sendNotificationEmail(
                    recipient.getUserId(),
                    notification.getTitle(),
                    notification.getMessage()
            );
            addDeliveryLog(notification, recipient.getUserId(), DeliveryType.EMAIL, DeliveryStatus.SUCCESS, null);
        } catch (Exception e) {
            log.error("Failed to send email to user {}: {}", recipient.getUserId(), e.getMessage());
            addDeliveryLog(notification, recipient.getUserId(), DeliveryType.EMAIL, DeliveryStatus.FAILED, e.getMessage());
        }
    }

    private void addDeliveryLog(Notification notification, UUID userId, DeliveryType type, DeliveryStatus status, String errorMessage) {
        NotificationDeliveryLog deliveryLog = NotificationDeliveryLog.builder()
                .notification(notification)
                .userId(userId)
                .deliveryType(type)
                .status(status)
                .attemptedAt(LocalDateTime.now())
                .errorMessage(errorMessage)
                .build();
        notification.getDeliveryLogs().add(deliveryLog);
    }

    private void addActivityLog(Notification notification, String action) {
        NotificationActivityLog activityLog = NotificationActivityLog.builder()
                .notification(notification)
                .action(action)
                .build();
        notification.getActivityLogs().add(activityLog);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return recipientRepository.findByUserIdOrderByDeliveredAtDesc(userId, pageable)
                .map(recipient -> notificationMapper.toUserNotificationResponse(
                        recipient.getNotification(), recipient));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotificationResponse> getUnreadUserNotifications(UUID userId, Pageable pageable) {
        return recipientRepository.findByUserIdAndReadFalseOrderByDeliveredAtDesc(userId, pageable)
                .map(recipient -> notificationMapper.toUserNotificationResponse(
                        recipient.getNotification(), recipient));
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {
        NotificationRecipient recipient = recipientRepository
                .findByNotification_NotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found for user: " + userId));

        if (!recipient.isRead()) {
            recipient.setRead(true);
            recipient.setReadAt(LocalDateTime.now());
            recipientRepository.save(recipient);
            log.info("Marked notification {} as read for user {}", notificationId, userId);
        }
    }

    @Override
    public void markAllAsRead(UUID userId) {
        recipientRepository.markAllAsReadByUserId(userId);
        log.info("Marked all notifications as read for user {}", userId);
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        notificationRepository.delete(notification);
        log.info("Deleted notification: {}", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return recipientRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatisticsResponse getStatistics() {
        long total = notificationRepository.count();
        long sent = notificationRepository.countByStatus(NotificationStatus.SENT);
        long failed = notificationRepository.countByStatus(NotificationStatus.FAILED);
        long pending = notificationRepository.countByStatus(NotificationStatus.CREATED);
        long unread = recipientRepository.countByReadFalse();

        return NotificationStatisticsResponse.builder()
                .totalNotifications(total)
                .sentNotifications(sent)
                .failedNotifications(failed)
                .pendingNotifications(pending)
                .totalUnread(unread)
                .build();
    }

    @Override
    public NotificationResponse processEvent(NotificationEvent event) {
        log.info("Processing notification event: {}", event.getEventType());

        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title(event.getEventType())
                .message(event.getEventType())
                .type(NotificationType.valueOf(event.getEventType()))
                .referenceId(event.getReferenceId())
                .referenceType(event.getReferenceType())
                .recipientUserIds(event.getRecipientUserIds())
                .build();

        NotificationResponse response = createNotification(request);
        sendNotification(response.getNotificationId());

        return response;
    }

    @Override
    public void broadcastNotification(BroadcastNotificationRequest request) {
        log.info("Broadcasting notification: {}", request.getTitle());

        CreateNotificationRequest createRequest = CreateNotificationRequest.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .recipientUserIds(request.getUserIds())
                .build();

        NotificationResponse response = createNotification(createRequest);
        sendNotification(response.getNotificationId());
    }
}
