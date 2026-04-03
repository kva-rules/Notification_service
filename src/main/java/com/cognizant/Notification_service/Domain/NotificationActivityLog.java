package com.cognizant.Notification_service.Domain;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "notification_activity_Log")
@Data
public class NotificationActivityLog {
    @Id
    @GeneratedValue
    @Column(name = "activityId", updatable = false, nullable = false)
    private UUID activityId;

    @JoinColumn(name = "notificationId", referencedColumnName = "notificationId", nullable = false)
    private UUID notificationId;

    @Column(nullable = false)
    private String action;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdAt;
}


