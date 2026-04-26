package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.UpdatePreferenceRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;
import com.cognizant.notificationservice.application.service.NotificationPreferenceService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Per-user channel preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Get user notification preferences", description = "Fetch stored channel prefs for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Preferences not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationPreferenceResponse> getPreferences(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        NotificationPreferenceResponse response = preferenceService.getPreferencesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Update user notification preferences", description = "Modify channel preferences for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Preferences not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationPreferenceResponse> updatePreferences(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        NotificationPreferenceResponse response = preferenceService.updatePreferences(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Create default preferences", description = "Initialize default channel prefs for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Default preferences created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Preferences already exist")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationPreferenceResponse> createDefaultPreferences(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        NotificationPreferenceResponse response = preferenceService.createDefaultPreferences(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
