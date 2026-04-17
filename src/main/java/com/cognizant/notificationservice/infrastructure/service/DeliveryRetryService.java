package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.service.EmailService;
import com.cognizant.notificationservice.domain.entity.NotificationDeliveryLog;
import com.cognizant.notificationservice.domain.enums.DeliveryStatus;
import com.cognizant.notificationservice.infrastructure.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryRetryService {

    private static final int MAX_RETRIES = 3;

    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "${app.retry.interval-ms:60000}")
    @Transactional
    public void retryFailedDeliveries() {
        log.debug("Starting retry for failed deliveries");

        List<NotificationDeliveryLog> failedDeliveries = deliveryLogRepository
                .findByStatusAndRetryCountLessThan(DeliveryStatus.FAILED, MAX_RETRIES);

        for (NotificationDeliveryLog delivery : failedDeliveries) {
            retryDelivery(delivery);
        }

        log.debug("Completed retry cycle, processed {} deliveries", failedDeliveries.size());
    }

    private void retryDelivery(NotificationDeliveryLog delivery) {
        log.info("Retrying delivery {} (attempt {})", delivery.getDeliveryId(), delivery.getRetryCount() + 1);

        try {
            switch (delivery.getDeliveryType()) {
                case EMAIL -> retryEmailDelivery(delivery);
                case IN_APP -> retryInAppDelivery(delivery);
            }

            delivery.setStatus(DeliveryStatus.SUCCESS);
            delivery.setErrorMessage(null);
            log.info("Retry successful for delivery {}", delivery.getDeliveryId());

        } catch (Exception e) {
            delivery.setRetryCount(delivery.getRetryCount() + 1);
            delivery.setErrorMessage(e.getMessage());
            delivery.setAttemptedAt(LocalDateTime.now());

            if (delivery.getRetryCount() >= MAX_RETRIES) {
                delivery.setStatus(DeliveryStatus.PERMANENTLY_FAILED);
                log.error("Delivery {} permanently failed after {} attempts", 
                        delivery.getDeliveryId(), MAX_RETRIES);
            } else {
                log.warn("Retry failed for delivery {}, attempt {}: {}", 
                        delivery.getDeliveryId(), delivery.getRetryCount(), e.getMessage());
            }
        }

        deliveryLogRepository.save(delivery);
    }

    private void retryEmailDelivery(NotificationDeliveryLog delivery) {
        emailService.sendNotificationEmail(
                delivery.getUserId(),
                delivery.getNotification().getTitle(),
                delivery.getNotification().getMessage()
        );
    }

    private void retryInAppDelivery(NotificationDeliveryLog delivery) {
        // In-app delivery retry logic - typically just mark as delivered
        // Real-time push would be handled by WebSocket
        log.info("In-app delivery retry for user {}", delivery.getUserId());
    }
}
