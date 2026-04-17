package com.cognizant.notificationservice.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatisticsResponse {
    private long totalNotifications;
    private long sentNotifications;
    private long failedNotifications;
    private long pendingNotifications;
    private long totalUnread;
}
