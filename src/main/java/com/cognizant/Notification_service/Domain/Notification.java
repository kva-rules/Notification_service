package com.cognizant.Notification_service.Domain;

import com.cognizant.Notification_service.enums.Status;
import com.cognizant.Notification_service.enums.Type;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "Notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue
    @Column(name = "notificationId", updatable = false, nullable = false)
    private UUID notificationId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String Type;

    @Column(nullable = false)
    private UUID referenceId;

    @Column(nullable = false)
    private String referenceType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
}
