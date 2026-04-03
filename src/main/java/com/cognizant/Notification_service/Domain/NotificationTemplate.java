package com.cognizant.Notification_service.Domain;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "NotificationTemplates")
@Data
public class NotificationTemplate {
    @Id
    @GeneratedValue
    @Column(name = "templateId", updatable = false, nullable = false)
    private UUID templateId;


    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String titleTemplate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String messageTemplate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdAt;
}








