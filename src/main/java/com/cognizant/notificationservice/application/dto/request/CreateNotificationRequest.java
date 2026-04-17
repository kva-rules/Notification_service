package com.cognizant.notificationservice.application.dto.request;

import com.cognizant.notificationservice.domain.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotNull(message = "Reference ID is required")
    private UUID referenceId;

    @NotBlank(message = "Reference type is required")
    private String referenceType;

    @NotEmpty(message = "At least one recipient is required")
    private List<UUID> recipientUserIds;
}
