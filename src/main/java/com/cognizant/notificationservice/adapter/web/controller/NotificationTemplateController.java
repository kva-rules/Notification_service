package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.CreateTemplateRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationTemplateResponse;
import com.cognizant.notificationservice.application.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Notification Templates", description = "CRUD for email/in-app templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a notification template", description = "Admin registers a new template")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Template created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationTemplateResponse> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        NotificationTemplateResponse response = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get template by ID", description = "Fetch a single template by identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationTemplateResponse> getTemplateById(
            @Parameter(description = "Template ID") @PathVariable UUID templateId) {
        NotificationTemplateResponse response = templateService.getTemplateById(templateId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event-type/{eventType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get template by event type", description = "Lookup a template mapped to an event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationTemplateResponse> getTemplateByEventType(
            @Parameter(description = "Event type key") @PathVariable String eventType) {
        NotificationTemplateResponse response = templateService.getTemplateByEventType(eventType);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all templates", description = "Return every registered notification template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificationTemplateResponse>> getAllTemplates() {
        List<NotificationTemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a template", description = "Modify an existing notification template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationTemplateResponse> updateTemplate(
            @Parameter(description = "Template ID") @PathVariable UUID templateId,
            @Valid @RequestBody CreateTemplateRequest request) {
        NotificationTemplateResponse response = templateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a template", description = "Remove a template permanently")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Template deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteTemplate(
            @Parameter(description = "Template ID") @PathVariable UUID templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
