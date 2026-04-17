package com.cognizant.notificationservice.infrastructure.repository;

import com.cognizant.notificationservice.domain.entity.NotificationDeliveryLog;
import com.cognizant.notificationservice.domain.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, UUID> {

    List<NotificationDeliveryLog> findByNotification_NotificationIdOrderByAttemptedAtDesc(UUID notificationId);

    List<NotificationDeliveryLog> findByUserIdOrderByAttemptedAtDesc(UUID userId);

    List<NotificationDeliveryLog> findByStatusAndRetryCountLessThan(DeliveryStatus status, int maxRetries);
}
