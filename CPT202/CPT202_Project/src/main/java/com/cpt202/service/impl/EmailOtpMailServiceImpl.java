package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.service.EmailOtpMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends email OTPs for passwordless login.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailOtpMailServiceImpl implements EmailOtpMailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.email-login-otp-expiration-minutes:5}")
    private long expirationMinutes;

    @Override
    public void sendLoginOtpMail(User user, String otp) {
        if (!StringUtils.hasText(mailFrom)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_NOT_CONFIGURED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("CPT202 Login Verification Code");
        message.setText(buildLoginMailContent(user, otp));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send login OTP mail to {}", user.getEmail(), ex);
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_SEND_FAILED, ex);
        }
    }

    @Override
    public void sendRegisterOtpMail(String email, String otp) {
        if (!StringUtils.hasText(mailFrom)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_NOT_CONFIGURED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("CPT202 Register Verification Code");
        message.setText(buildRegisterMailContent(otp));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send register OTP mail to {}", email, ex);
            throw new BusinessException(MessageConstants.EMAIL_OTP_MAIL_SEND_FAILED, ex);
        }
    }

    private String buildLoginMailContent(User user, String otp) {
        String displayName = StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername();
        return """
                Hello %s,

                Your CPT202 Project Selection System login verification code is:

                %s

                This code will expire in %d minutes.
                If you did not request this login, you can safely ignore this email.
                """.formatted(displayName, otp, expirationMinutes);
    }

    private String buildRegisterMailContent(String otp) {
        return """
                Hello,

                Your CPT202 Project Selection System Register verification code is:

                %s

                This code will expire in %d minutes.
                If you did not request this register, you can safely ignore this email.
                """.formatted(otp, expirationMinutes);
    }
}
