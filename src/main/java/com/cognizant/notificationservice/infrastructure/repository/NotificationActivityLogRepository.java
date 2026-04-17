package com.cognizant.notificationservice.infrastructure.repository;

import com.cognizant.notificationservice.domain.entity.NotificationActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationActivityLogRepository extends JpaRepository<NotificationActivityLog, UUID> {

    List<NotificationActivityLog> findByNotification_NotificationIdOrderByCreatedAtDesc(UUID notificationId);
}
