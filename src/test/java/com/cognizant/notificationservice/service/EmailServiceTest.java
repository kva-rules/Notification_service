package com.cognizant.notificationservice.service;

import com.cognizant.notificationservice.infrastructure.service.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    @Test
    @DisplayName("Should send simple email successfully")
    void sendEmail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not send email when disabled")
    void sendEmail_Disabled() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        emailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send templated email")
    void sendTemplatedEmail_Success() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
        doNothing().when(mailSender).send(any(jakarta.mail.internet.MimeMessage.class));
        when(mailSender.createMimeMessage()).thenReturn(mock(jakarta.mail.internet.MimeMessage.class));

        Map<String, String> variables = Map.of("ticketId", "TKT-001", "message", "Test message");

        emailService.sendTemplatedEmail("recipient@example.com", "TICKET_CREATED", variables);

        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("Should not send notification email when user email not found")
    void sendNotificationEmail_NoEmail() {
        UUID userId = UUID.randomUUID();

        emailService.sendNotificationEmail(userId, "Test Title", "Test Message");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
