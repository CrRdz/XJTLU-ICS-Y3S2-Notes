package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.service.PasswordResetMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends forgot-password emails.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.password-reset-token-expiration-minutes:30}")
    private long expirationMinutes;

    @Override
    public void sendPasswordResetMail(User user, String resetLink) {
        if (!StringUtils.hasText(mailFrom)) {
            throw new BusinessException(MessageConstants.PASSWORD_RESET_MAIL_NOT_CONFIGURED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("CPT202 Password Reset");
        message.setText(buildMailContent(user, resetLink));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send password reset mail to {}", user.getEmail(), ex);
            throw new BusinessException(MessageConstants.PASSWORD_RESET_MAIL_SEND_FAILED, ex);
        }
    }

    private String buildMailContent(User user, String resetLink) {
        String displayName = StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername();
        return """
                Hello %s,

                We received a request to reset your password for the CPT202 Project Selection System.

                Please open the link below and complete the reset within %d minutes:
                %s

                If you did not request this change, you can safely ignore this email.
                """.formatted(displayName, expirationMinutes, resetLink);
    }
}
