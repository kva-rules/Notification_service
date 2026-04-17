package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.CreateTemplateRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
@Tag(name = "Notification Templates", description = "Notification template management APIs (Admin only)")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new notification template")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        NotificationTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<NotificationTemplateResponse> getTemplateById(
            @PathVariable UUID templateId) {
        NotificationTemplateResponse response = templateService.getTemplateById(templateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-type/{eventType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get template by event type")
    public ResponseEntity<NotificationTemplateResponse> getTemplateByEventType(
            @PathVariable String eventType) {
        NotificationTemplateResponse response = templateService.getTemplateByEventType(eventType);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all templates")
    public ResponseEntity<List<NotificationTemplateResponse>> getAllTemplates() {
        List<NotificationTemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a template")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody CreateTemplateRequest request) {
        NotificationTemplateResponse response = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
