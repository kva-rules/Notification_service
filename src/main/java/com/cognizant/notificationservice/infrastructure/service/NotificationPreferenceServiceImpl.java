package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.dto.request.UpdatePreferenceRequest;
import com.cognizant.notificationservice.application.dto.response.NotificationPreferenceResponse;
import com.cognizant.notificationservice.application.mapper.NotificationPreferenceMapper;
import com.cognizant.notificationservice.application.service.NotificationPreferenceService;
import com.cognizant.notificationservice.domain.entity.NotificationPreference;
import com.cognizant.notificationservice.domain.exception.NotificationPreferenceNotFoundException;
import com.cognizant.notificationservice.infrastructure.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationPreferenceMapper preferenceMapper;

    @Override
    @Transactional(readOnly = true, noRollbackFor = NotificationPreferenceNotFoundException.class)
    public NotificationPreferenceResponse getPreferencesByUserId(UUID userId) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new NotificationPreferenceNotFoundException(userId));
        return preferenceMapper.toResponse(preference);
    }

    @Override
    public NotificationPreferenceResponse createDefaultPreferences(UUID userId) {
        if (preferenceRepository.existsByUserId(userId)) {
            log.warn("Preferences already exist for user: {}", userId);
            return getPreferencesByUserId(userId);
        }

        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .ticketUpdates(true)
                .solutionUpdates(true)
                .knowledgeUpdates(true)
                .rewardUpdates(true)
                .build();

        NotificationPreference savedPreference = preferenceRepository.save(preference);
        log.info("Created default preferences for user: {}", userId);

        return preferenceMapper.toResponse(savedPreference);
    }

    @Override
    public NotificationPreferenceResponse updatePreferences(UUID userId, UpdatePreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new NotificationPreferenceNotFoundException(userId));

        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        if (request.getTicketUpdates() != null) {
            preference.setTicketUpdates(request.getTicketUpdates());
        }
        if (request.getSolutionUpdates() != null) {
            preference.setSolutionUpdates(request.getSolutionUpdates());
        }
        if (request.getKnowledgeUpdates() != null) {
            preference.setKnowledgeUpdates(request.getKnowledgeUpdates());
        }
        if (request.getRewardUpdates() != null) {
            preference.setRewardUpdates(request.getRewardUpdates());
        }

        NotificationPreference updatedPreference = preferenceRepository.save(preference);
        log.info("Updated preferences for user: {}", userId);

        return preferenceMapper.toResponse(updatedPreference);
    }
}
