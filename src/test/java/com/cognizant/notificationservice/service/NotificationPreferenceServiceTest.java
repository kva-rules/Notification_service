package com.cognizant.notificationservice.service;

import com.cognizant.notificationservice.application.dto.request.UpdatePreferenceRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;
import com.cognizant.notificationservice.application.mapper.NotificationPreferenceMapper;
import com.cognizant.notificationservice.domain.entity.NotificationPreference;
import com.cognizant.notificationservice.domain.exception.NotificationPreferenceNotFoundException;
import com.cognizant.notificationservice.infrastructure.repository.NotificationPreferenceRepository;
import com.cognizant.notificationservice.infrastructure.service.NotificationPreferenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private NotificationPreferenceMapper preferenceMapper;

    @InjectMocks
    private NotificationPreferenceServiceImpl preferenceService;

    private UUID userId;
    private NotificationPreference preference;
    private NotificationPreferenceResponse preferenceResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        preference = NotificationPreference.builder()
                .preferenceId(UUID.randomUUID())
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .ticketUpdates(true)
                .solutionUpdates(true)
                .knowledgeUpdates(true)
                .rewardUpdates(true)
                .build();

        preferenceResponse = NotificationPreferenceResponse.builder()
                .preferenceId(preference.getPreferenceId())
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .ticketUpdates(true)
                .solutionUpdates(true)
                .knowledgeUpdates(true)
                .rewardUpdates(true)
                .build();
    }

    @Test
    @DisplayName("Should get preferences by user ID")
    void getPreferencesByUserId_Success() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.getPreferencesByUserId(userId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.isEmailEnabled()).isTrue();
        verify(preferenceRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should throw exception when preferences not found")
    void getPreferencesByUserId_NotFound() {
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> preferenceService.getPreferencesByUserId(userId))
                .isInstanceOf(NotificationPreferenceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create default preferences")
    void createDefaultPreferences_Success() {
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.createDefaultPreferences(userId);

        assertThat(result).isNotNull();
        assertThat(result.isEmailEnabled()).isTrue();
        assertThat(result.isInAppEnabled()).isTrue();
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("Should update preferences")
    void updatePreferences_Success() {
        UpdatePreferenceRequest updateRequest = UpdatePreferenceRequest.builder()
                .emailEnabled(false)
                .ticketUpdates(false)
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(preference);
        when(preferenceMapper.toResponse(preference)).thenReturn(preferenceResponse);

        NotificationPreferenceResponse result = preferenceService.updatePreferences(userId, updateRequest);

        assertThat(result).isNotNull();
        verify(preferenceRepository).save(preference);
    }
}
