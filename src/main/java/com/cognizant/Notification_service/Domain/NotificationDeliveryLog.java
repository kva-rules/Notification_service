package com.cognizant.Notification_service.Domain;
import com.cognizant.Notification_service.enums.DeliveryType;
import com.cognizant.Notification_service.enums.Statuss;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity(name = "NotificationDeliveryLog")
@Data
public class NotificationDeliveryLog {
    @Id
    @GeneratedValue
    @Column(name = "deliveryId", updatable = false, nullable = false)
    private UUID deliveryId;

    @JoinColumn(name = "notificationId", referencedColumnName = "notificationId", nullable = false)
    private UUID notificationId;

    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statuss status;

    @Column(nullable = false)
    private Date attemptedAt;
}

