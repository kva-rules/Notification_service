package com.cognizant.notificationservice.application.dto.response;

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
public class NotificationRecipientResponse {

    private UUID recipientId;
    private UUID notificationId;
    private UUID userId;
    private boolean read;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
}
