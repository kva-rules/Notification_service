package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import com.cognizant.notificationservice.application.mapper.NotificationMapper;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.entity.NotificationActivityLog;
import com.cognizant.notificationservice.domain.entity.NotificationRecipient;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
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
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return recipientRepository.countByUserIdAndReadFalse(userId);
    }
}
