package com.cognizant.notificationservice.service;

import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.mapper.NotificationMapper;
import com.cognizant.notificationservice.application.service.EmailService;
import com.cognizant.notificationservice.application.service.NotificationPreferenceService;
import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.entity.NotificationRecipient;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.domain.exception.NotificationNotFoundException;
import com.cognizant.notificationservice.infrastructure.repository.NotificationRecipientRepository;
import com.cognizant.notificationservice.infrastructure.repository.NotificationRepository;
import com.cognizant.notificationservice.infrastructure.service.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRecipientRepository recipientRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationPreferenceService preferenceService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID notificationId;
    private UUID userId;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private CreateNotificationRequest createRequest;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        notification = Notification.builder()
                .notificationId(notificationId)
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .status(NotificationStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .recipients(new ArrayList<>())
                .activityLogs(new ArrayList<>())
                .deliveryLogs(new ArrayList<>())
                .build();

        notificationResponse = NotificationResponse.builder()
                .notificationId(notificationId)
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .status(NotificationStatus.CREATED)
                .build();

        createRequest = CreateNotificationRequest.builder()
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .recipientUserIds(List.of(userId))
                .build();
    }

    @Test
    @DisplayName("Should create notification successfully")
    void createNotification_Success() {
        when(notificationMapper.toEntity(any(CreateNotificationRequest.class))).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getNotificationId()).isEqualTo(notificationId);
        assertThat(result.getTitle()).isEqualTo("Test Notification");
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should get notification by ID successfully")
    void getNotificationById_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.getNotificationById(notificationId);

        assertThat(result).isNotNull();
        assertThat(result.getNotificationId()).isEqualTo(notificationId);
        verify(notificationRepository).findById(notificationId);
    }

    @Test
    @DisplayName("Should throw exception when notification not found")
    void getNotificationById_NotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    @DisplayName("Should mark notification as read")
    void markAsRead_Success() {
        NotificationRecipient recipient = NotificationRecipient.builder()
                .recipientId(UUID.randomUUID())
                .notification(notification)
                .userId(userId)
                .read(false)
                .build();

        when(recipientRepository.findByNotification_NotificationIdAndUserId(notificationId, userId))
                .thenReturn(Optional.of(recipient));

        notificationService.markAsRead(notificationId, userId);

        assertThat(recipient.isRead()).isTrue();
        assertThat(recipient.getReadAt()).isNotNull();
        verify(recipientRepository).save(recipient);
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void markAllAsRead_Success() {
        notificationService.markAllAsRead(userId);

        verify(recipientRepository).markAllAsReadByUserId(userId);
    }

    @Test
    @DisplayName("Should get unread count")
    void getUnreadCount_Success() {
        when(recipientRepository.countByUserIdAndReadFalse(userId)).thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
        verify(recipientRepository).countByUserIdAndReadFalse(userId);
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void deleteNotification_Success() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId);

        verify(notificationRepository).delete(notification);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent notification")
    void deleteNotification_NotFound() {
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
