package com.cognizant.notificationservice.application.service;

import com.cognizant.notificationservice.application.dto.event.NotificationEvent;
import com.cognizant.notificationservice.application.dto.request.BroadcastNotificationRequest;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationStatisticsResponse;
import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    void sendNotification(UUID notificationId);

    NotificationResponse getNotificationById(UUID notificationId);

    Page<UserNotificationResponse> getUserNotifications(UUID userId, Pageable pageable);

    Page<UserNotificationResponse> getUnreadUserNotifications(UUID userId, Pageable pageable);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId);

    long getUnreadCount(UUID userId);

    NotificationStatisticsResponse getStatistics();

    NotificationResponse processEvent(NotificationEvent event);

    void broadcastNotification(BroadcastNotificationRequest request);
}
