package com.cognizant.Notification_service.Domain;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity(name = "NotificationPreferences")
@Data
public class NotificationPreference {

    @Id
    @GeneratedValue
    @Column(name = "preferenceId", updatable = false, nullable = false)
    private UUID preferenceId;

    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private boolean emailEnabled;

    @Column(nullable = false)
    private boolean inAppEnabled;

    @Column(nullable = false)
    private boolean ticketUpdates;

    @Column(nullable = false)
    private boolean solutionUpdates;

    @Column(nullable = false)
    private boolean knowledgeUpdates;

    @Column(nullable = false)
    private boolean rewardUpdates;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Date createdAt;



}




