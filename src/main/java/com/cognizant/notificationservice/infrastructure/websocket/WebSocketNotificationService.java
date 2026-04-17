package com.cognizant.notificationservice.infrastructure.websocket;

import com.cognizant.notificationservice.application.dto.response.UserNotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(UUID userId, UserNotificationResponse notification) {
        log.info("Sending WebSocket notification to user: {}", userId);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }

    public void broadcastNotification(UserNotificationResponse notification) {
        log.info("Broadcasting notification via WebSocket");
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    public void sendUnreadCount(UUID userId, long count) {
        log.info("Sending unread count {} to user: {}", count, userId);
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/unread-count",
                count
        );
    }
}
