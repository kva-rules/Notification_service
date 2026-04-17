package com.cognizant.notificationservice.application.dto.response;

import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID notificationId;
    private String title;
    private String message;
    private NotificationType type;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
    private NotificationStatus status;
}
