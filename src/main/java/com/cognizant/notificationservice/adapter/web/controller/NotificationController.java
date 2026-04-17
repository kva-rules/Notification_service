package com.cognizant.notificationservice.adapter.web.controller;

import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a new notification")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable UUID notificationId) {
        NotificationResponse response = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @Operation(summary = "Get all notifications for the authenticated user")
    public ResponseEntity<Page<UserNotificationResponse>> getUserNotifications(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserNotificationResponse> notifications =
                notificationService.getUserNotifications(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/unread")
    @Operation(summary = "Get unread notifications for the authenticated user")
    public ResponseEntity<Page<UserNotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserNotificationResponse> notifications =
                notificationService.getUnreadUserNotifications(UUID.fromString(userId), pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/unread/count")
    @Operation(summary = "Get unread notification count for the authenticated user")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal String userId) {
        long count = notificationService.getUnreadCount(UUID.fromString(userId));
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal String userId) {
        notificationService.markAsRead(notificationId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/read-all")
    @Operation(summary = "Mark all notifications as read for the authenticated user")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal String userId) {
        notificationService.markAllAsRead(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
