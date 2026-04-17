package com.cognizant.notificationservice.infrastructure.repository;

import com.cognizant.notificationservice.domain.entity.NotificationRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, UUID> {

    Page<NotificationRecipient> findByUserIdOrderByDeliveredAtDesc(UUID userId, Pageable pageable);

    Page<NotificationRecipient> findByUserIdAndReadFalseOrderByDeliveredAtDesc(UUID userId, Pageable pageable);

    Optional<NotificationRecipient> findByNotification_NotificationIdAndUserId(UUID notificationId, UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.read = true, nr.readAt = CURRENT_TIMESTAMP WHERE nr.userId = :userId AND nr.read = false")
    void markAllAsReadByUserId(@Param("userId") UUID userId);

    long countByReadFalse();
}
