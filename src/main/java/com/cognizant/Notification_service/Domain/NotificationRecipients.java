package com.cognizant.Notification_service.Domain;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "NotificationRecipients")
@Data
public class NotificationRecipients

{
        @Id
        @GeneratedValue
        @Column(name = "recipientId", updatable = false, nullable = false)
        private UUID recipientId;

        @JoinColumn(name = "notificationId", referencedColumnName = "notificationId", nullable = false)
        private UUID notificationId;

        @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
        private UUID userId;

        @Column(nullable = false)
        private boolean isRead;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private Date deliveredAt;

        private Date readAt;

}
