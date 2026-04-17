package com.cognizant.notificationservice.controller;

import com.cognizant.notificationservice.adapter.web.controller.NotificationController;
import com.cognizant.notificationservice.application.dto.request.CreateNotificationRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationResponse;
import com.cognizant.notificationservice.application.dto.response.NotificationStatisticsResponse;
import com.cognizant.notificationservice.application.service.NotificationService;
import com.cognizant.notificationservice.domain.enums.NotificationStatus;
import com.cognizant.notificationservice.domain.enums.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private UUID notificationId;
    private UUID userId;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        notificationResponse = NotificationResponse.builder()
                .notificationId(notificationId)
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .status(NotificationStatus.CREATED)
                .build();
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should get notification by ID")
    void getNotificationById_Success() throws Exception {
        when(notificationService.getNotificationById(notificationId)).thenReturn(notificationResponse);

        mockMvc.perform(get("/api/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notificationId.toString()))
                .andExpect(jsonPath("$.title").value("Test Notification"));

        verify(notificationService).getNotificationById(notificationId);
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should get unread count for user")
    void getUnreadCount_Success() throws Exception {
        when(notificationService.getUnreadCount(userId)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/users/{userId}/unread-count", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(notificationService).getUnreadCount(userId);
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should mark notification as read")
    void markAsRead_Success() throws Exception {
        doNothing().when(notificationService).markAsRead(notificationId, userId);

        mockMvc.perform(put("/api/notifications/{id}/read", notificationId)
                        .param("userId", userId.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAsRead(notificationId, userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create notification as admin")
    void createNotification_AsAdmin_Success() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .recipientUserIds(List.of(userId))
                .build();

        when(notificationService.createNotification(any())).thenReturn(notificationResponse);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value(notificationId.toString()));

        verify(notificationService).createNotification(any());
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should deny create notification for engineer")
    void createNotification_AsEngineer_Forbidden() throws Exception {
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .title("Test Notification")
                .message("Test Message")
                .type(NotificationType.SYSTEM_ALERT)
                .recipientUserIds(List.of(userId))
                .build();

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get statistics as admin")
    void getStatistics_AsAdmin_Success() throws Exception {
        NotificationStatisticsResponse stats = NotificationStatisticsResponse.builder()
                .totalNotifications(100)
                .sentNotifications(80)
                .failedNotifications(5)
                .pendingNotifications(15)
                .totalUnread(50)
                .build();

        when(notificationService.getStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/notifications/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotifications").value(100))
                .andExpect(jsonPath("$.sentNotifications").value(80));

        verify(notificationService).getStatistics();
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should delete notification")
    void deleteNotification_Success() throws Exception {
        doNothing().when(notificationService).deleteNotification(notificationId);

        mockMvc.perform(delete("/api/notifications/{id}", notificationId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(notificationId);
    }
}
