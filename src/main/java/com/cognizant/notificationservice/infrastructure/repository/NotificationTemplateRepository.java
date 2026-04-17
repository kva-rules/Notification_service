package com.cognizant.notificationservice.infrastructure.repository;

import com.cognizant.notificationservice.domain.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByEventType(String eventType);

    boolean existsByEventType(String eventType);
}
