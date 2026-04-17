package com.cognizant.notificationservice.service;

import com.cognizant.notificationservice.application.service.EmailService;
import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.entity.NotificationDeliveryLog;
import com.cognizant.notificationservice.domain.enums.DeliveryStatus;
import com.cognizant.notificationservice.domain.enums.DeliveryType;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.cognizant.notificationservice.infrastructure.repository.NotificationDeliveryLogRepository;
import com.cognizant.notificationservice.infrastructure.service.DeliveryRetryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryRetryServiceTest {

    @Mock
    private NotificationDeliveryLogRepository deliveryLogRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeliveryRetryService retryService;

    private NotificationDeliveryLog failedDelivery;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .build();

        failedDelivery = NotificationDeliveryLog.builder()
                .deliveryId(UUID.randomUUID())
                .notification(notification)
                .userId(UUID.randomUUID())
                .deliveryType(DeliveryType.EMAIL)
                .status(DeliveryStatus.FAILED)
                .attemptedAt(LocalDateTime.now())
                .retryCount(0)
                .errorMessage("Connection timeout")
                .build();
    }

    @Test
    @DisplayName("Should retry failed deliveries")
    void retryFailedDeliveries_Success() {
        when(deliveryLogRepository.findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, 3))
                .thenReturn(List.of(failedDelivery));
        doNothing().when(emailService).sendNotificationEmail(any(), any(), any());
        when(deliveryLogRepository.save(any())).thenReturn(failedDelivery);

        retryService.retryFailedDeliveries();

        verify(deliveryLogRepository).findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, 3);
        verify(deliveryLogRepository).save(any(NotificationDeliveryLog.class));
    }

    @Test
    @DisplayName("Should mark as permanently failed after max retries")
    void retryFailedDeliveries_MaxRetriesReached() {
        failedDelivery.setRetryCount(2);
        
        when(deliveryLogRepository.findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, 3))
                .thenReturn(List.of(failedDelivery));
        doThrow(new RuntimeException("Email server down")).when(emailService)
                .sendNotificationEmail(any(), any(), any());
        when(deliveryLogRepository.save(any())).thenReturn(failedDelivery);

        retryService.retryFailedDeliveries();

        verify(deliveryLogRepository).save(argThat(log -> 
                log.getStatus() == DeliveryStatus.PERMANENTLY_FAILED));
    }

    @Test
    @DisplayName("Should handle empty failed deliveries list")
    void retryFailedDeliveries_NoFailedDeliveries() {
        when(deliveryLogRepository.findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, 3))
                .thenReturn(List.of());

        retryService.retryFailedDeliveries();

        verify(deliveryLogRepository).findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, 3);
        verify(deliveryLogRepository, never()).save(any());
    }
}
