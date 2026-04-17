package com.cognizant.notificationservice.infrastructure.service;

import com.cognizant.notificationservice.application.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@notification-service.com}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send to: {}, subject: {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendTemplatedEmail(String to, String templateName, Map<String, String> variables) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send templated email to: {}, template: {}", to, templateName);
            return;
        }

        try {
            String subject = processTemplate(getTemplateSubject(templateName), variables);
            String body = processTemplate(getTemplateBody(templateName), variables);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            log.info("Templated email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send templated email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send templated email", e);
        }
    }

    @Override
    public void sendNotificationEmail(UUID userId, String title, String message) {
        if (!emailEnabled) {
            log.info("Email sending disabled. Would send notification email for user: {}, title: {}", userId, title);
            return;
        }

        String userEmail = getUserEmail(userId);
        if (userEmail == null) {
            log.warn("No email found for user: {}", userId);
            return;
        }

        String htmlBody = buildNotificationEmailBody(title, message);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(userEmail);
            helper.setSubject(title);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("Notification email sent to user: {}", userId);

        } catch (MessagingException e) {
            log.error("Failed to send notification email to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to send notification email", e);
        }
    }

    private String processTemplate(String template, Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    private String getTemplateSubject(String templateName) {
        return switch (templateName) {
            case "TICKET_CREATED" -> "New Ticket Created: {{ticketId}}";
            case "TICKET_ASSIGNED" -> "Ticket Assigned to You: {{ticketId}}";
            case "TICKET_RESOLVED" -> "Ticket Resolved: {{ticketId}}";
            case "SOLUTION_CREATED" -> "New Solution Posted";
            case "SOLUTION_APPROVED" -> "Your Solution was Approved";
            case "KNOWLEDGE_CREATED" -> "New Knowledge Article Available";
            case "REWARD_POINTS_ADDED" -> "You Earned Reward Points!";
            case "BADGE_AWARDED" -> "Congratulations! You Earned a Badge";
            case "LEADERBOARD_UPDATED" -> "Leaderboard Update";
            case "SYSTEM_ALERT" -> "System Alert";
            default -> "Notification";
        };
    }

    private String getTemplateBody(String templateName) {
        return switch (templateName) {
            case "TICKET_CREATED" -> "<p>A new ticket has been created with ID: <strong>{{ticketId}}</strong></p><p>{{message}}</p>";
            case "TICKET_ASSIGNED" -> "<p>Ticket <strong>{{ticketId}}</strong> has been assigned to you.</p><p>{{message}}</p>";
            case "TICKET_RESOLVED" -> "<p>Ticket <strong>{{ticketId}}</strong> has been resolved.</p><p>{{message}}</p>";
            default -> "<p>{{message}}</p>";
        };
    }

    private String buildNotificationEmailBody(String title, String message) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4A90D9; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                        </div>
                        <div class="content">
                            <p>%s</p>
                        </div>
                        <div class="footer">
                            <p>This is an automated notification. Please do not reply to this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(title, message);
    }

    private String getUserEmail(UUID userId) {
        // In a real implementation, this would fetch the user's email from a user service or database
        // For now, returning null to indicate email lookup is not implemented
        log.debug("User email lookup not implemented for userId: {}", userId);
        return null;
    }
}
