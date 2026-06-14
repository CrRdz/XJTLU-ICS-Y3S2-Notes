package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.security.TwoFactorLoginChallenge;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.TwoFactorAuthService;
import com.cpt202.util.TotpUtil;
import com.cpt202.vo.TwoFactorSetupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_TOTP_ISSUER = "CPT202 Project Selection System";
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;

    @Value("${app.frontend-base-url:http://localhost:${server.port}}")
    private String frontendBaseUrl;

    @Value("${app.totp-issuer:}")
    private String issuer;

    @Value("${app.two-factor-setup-expiration-minutes:10}")
    private long setupExpirationMinutes;

    @Value("${app.two-factor-login-challenge-expiration-minutes:5}")
    private long challengeExpirationMinutes;

    @Override
    public TwoFactorSetupVO initializeSetup(User user) {
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && StringUtils.hasText(user.getTwoFactorSecret())) {
            return TwoFactorSetupVO.builder()
                    .enabled(true)
                    .manualEntryKey(null)
                    .otpAuthUri(null)
                    .build();
        }

        String secret = TotpUtil.generateBase32Secret();
        redisCacheService.set(setupKey(user.getUserId()), secret, Duration.ofMinutes(setupExpirationMinutes));

        return TwoFactorSetupVO.builder()
                .enabled(false)
                .manualEntryKey(secret)
            .otpAuthUri(TotpUtil.buildOtpAuthUri(resolveOtpIssuer(), user.getEmail(), secret))
                .build();
    }

    @Override
    @Transactional
    public void enable(User user, String code) {
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && StringUtils.hasText(user.getTwoFactorSecret())) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_ALREADY_ENABLED);
        }
        String secret = redisCacheService.get(setupKey(user.getUserId()), String.class)
                .orElseThrow(() -> new BusinessException(MessageConstants.TWO_FACTOR_SETUP_EXPIRED));
        if (!TotpUtil.isValidCode(secret, code)) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_CODE_INVALID);
        }
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redisCacheService.delete(setupKey(user.getUserId()));
    }

    @Override
    @Transactional
    public void disable(User user) {
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        redisCacheService.delete(setupKey(user.getUserId()));
    }

    @Override
    public String createLoginChallenge(User user) {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisCacheService.set(challengeKey(token), new TwoFactorLoginChallenge(user.getUserId()),
                Duration.ofMinutes(challengeExpirationMinutes));
        return token;
    }

    @Override
    public User verifyLoginChallenge(String challengeToken, String code) {
        TwoFactorLoginChallenge challenge = redisCacheService.get(challengeKey(challengeToken), TwoFactorLoginChallenge.class)
                .orElseThrow(() -> new BusinessException(MessageConstants.TWO_FACTOR_CHALLENGE_INVALID));

        User user = userRepository.findById(challenge.getUserId())
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled()) || !StringUtils.hasText(user.getTwoFactorSecret())) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_NOT_ENABLED);
        }
        if (!TotpUtil.isValidCode(user.getTwoFactorSecret(), code)) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_CODE_INVALID);
        }

        redisCacheService.delete(challengeKey(challengeToken));
        return user;
    }

    private String setupKey(Long userId) {
        return RedisKeyConstants.TWO_FACTOR_SETUP_PREFIX + userId;
    }

    private String challengeKey(String token) {
        return RedisKeyConstants.TWO_FACTOR_LOGIN_CHALLENGE_PREFIX + token;
    }

    private String resolveOtpIssuer() {
        if (StringUtils.hasText(issuer)) {
            return issuer.trim();
        }

        String derivedIssuer = deriveIssuerFromBaseUrl(frontendBaseUrl);
        return StringUtils.hasText(derivedIssuer) ? derivedIssuer : DEFAULT_TOTP_ISSUER;
    }

    private String deriveIssuerFromBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return null;
        }

        String trimmed = baseUrl.trim();

        try {
            URI parsed = URI.create(trimmed);
            if (StringUtils.hasText(parsed.getHost())) {
                return parsed.getHost();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through and try parsing host-only values by prepending a scheme.
        }

        try {
            URI parsed = URI.create("https://" + trimmed);
            if (StringUtils.hasText(parsed.getHost())) {
                return parsed.getHost();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to the string-based fallback below.
        }

        int schemeIndex = trimmed.indexOf("://");
        String withoutScheme = schemeIndex >= 0 ? trimmed.substring(schemeIndex + 3) : trimmed;
        int pathIndex = withoutScheme.indexOf('/');
        String hostPort = pathIndex >= 0 ? withoutScheme.substring(0, pathIndex) : withoutScheme;
        int portIndex = hostPort.indexOf(':');
        return portIndex >= 0 ? hostPort.substring(0, portIndex) : hostPort;
    }
}
