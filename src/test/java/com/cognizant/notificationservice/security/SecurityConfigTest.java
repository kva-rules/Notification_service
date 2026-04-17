package com.cognizant.notificationservice.security;

import com.cognizant.notificationservice.application.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("Should allow access to actuator endpoints without authentication")
    void actuatorEndpoints_NoAuth_Success() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to swagger without authentication")
    void swaggerEndpoints_NoAuth_Success() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should deny access to API without authentication")
    void apiEndpoints_NoAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications/statistics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should allow ENGINEER to access user endpoints")
    void userEndpoints_Engineer_Success() throws Exception {
        mockMvc.perform(get("/api/notifications/users/123e4567-e89b-12d3-a456-426614174000/unread-count"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow ADMIN to access admin endpoints")
    void adminEndpoints_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/notifications/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ENGINEER")
    @DisplayName("Should deny ENGINEER access to admin endpoints")
    void adminEndpoints_Engineer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/notifications/statistics"))
                .andExpect(status().isForbidden());
    }
}
