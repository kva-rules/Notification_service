package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationStatisticsResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Send/list/mark-read notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== USER APIs (ENGINEER + ADMIN) ====================

    @GetMapping("/users/{userId}")
    // End users (ROLE_USER) need to read their own notifications — that's literally the
    // primary use-case of a notification feed. Locking this to ENGINEER/ADMIN broke the
    // demo flow (a freshly registered user would see 403 on /api/notifications/users/{me}).
    @PreAuthorize("hasAnyRole('USER', 'ENGINEER', 'MANAGER', 'ADMIN')")
    @Operation(summary = "List notifications for a user", description = "Paginated notifications owned by the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<UserNotificationResponse>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "deliveredAt") Pageable pageable) {
        Page<UserNotificationResponse> notifications =
                notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Get notification by ID", description = "Fetch a single notification by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification marked read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable UUID id,
            @Parameter(description = "User ID") @RequestParam UUID userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/read-all")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Mark all notifications as read", description = "Marks every unread notification for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All notifications marked read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Delete a notification", description = "Remove the notification permanently")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notification deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/unread-count")
    @PreAuthorize("hasAnyRole('ENGINEER', 'ADMIN')")
    @Operation(summary = "Get unread notification count", description = "Number of unread notifications for the user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Long> getUnreadCount(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    // ==================== ADMIN APIs ====================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a notification", description = "Admin creates a new notification record")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create and send a notification", description = "Admin creates and dispatches immediately")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notification created and sent"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationResponse> createAndSendNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        notificationService.sendNotification(response.getNotificationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send an existing notification", description = "Admin triggers dispatch for a stored record")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> sendNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {
        notificationService.sendNotification(id);
        return ResponseEntity.ok().build();
    }

    // ==================== STATISTICS API ====================

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification statistics", description = "Aggregate delivery counters for admins")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationStatisticsResponse> getStatistics() {
        NotificationStatisticsResponse statistics = notificationService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}
