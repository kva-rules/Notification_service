package com.cognizant.notificationservice.infrastructure.repository;

import com.cognizant.notificationservice.domain.entity.Notification;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.userId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.userId = :userId AND r.read = :isRead ORDER BY n.createdAt DESC")
    Page<Notification> findByIsRead(@Param("userId") UUID userId, @Param("isRead") boolean isRead, Pageable pageable);

    @Query("SELECT n FROM Notification n JOIN n.recipients r WHERE r.userId = :userId AND r.read = :isRead ORDER BY n.createdAt DESC")
    List<Notification> findByIsRead(@Param("userId") UUID userId, @Param("isRead") boolean isRead);

    @Query("SELECT COUNT(n) FROM Notification n JOIN n.recipients r WHERE r.userId = :userId AND r.read = false")
    long countUnread(@Param("userId") UUID userId);

    List<Notification> findByStatus(NotificationStatus status);

    long countByStatus(NotificationStatus status);
}
