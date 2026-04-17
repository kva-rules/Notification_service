package com.cognizant.notificationservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Event type is required")
    private String eventType;

    @NotBlank(message = "Title template is required")
    private String titleTemplate;

    @NotBlank(message = "Message template is required")
    private String messageTemplate;
}
