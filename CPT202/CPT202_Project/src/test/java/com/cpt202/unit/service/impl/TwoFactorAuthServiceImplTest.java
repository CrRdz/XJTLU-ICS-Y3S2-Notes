package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.security.TwoFactorLoginChallenge;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.impl.TwoFactorAuthServiceImpl;
import com.cpt202.util.TotpUtil;
import com.cpt202.vo.TwoFactorSetupVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for TOTP setup, enablement, and login-challenge verification. */
@ExtendWith(MockitoExtension.class)
class TwoFactorAuthServiceImplTest {

    private static final String FIXED_SECRET = "JBSWY3DPEHPK3PXP";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private TwoFactorAuthServiceImpl twoFactorAuthService;

    /** Configures deterministic issuer and TTL values for two-factor tests. */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(twoFactorAuthService, "issuer", "CPT202 Test");
        ReflectionTestUtils.setField(twoFactorAuthService, "frontendBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(twoFactorAuthService, "setupExpirationMinutes", 10L);
        ReflectionTestUtils.setField(twoFactorAuthService, "challengeExpirationMinutes", 5L);
    }

    /**
     * Setup initialization does not need a fake clock. The important contract is
     * that it caches a secret for later verification and returns an otpauth URI.
     */
    @Test
    void initializeSetupShouldCacheSecretAndReturnOtpUri() {
        User user = user(1L, "alice@example.com");

        TwoFactorSetupVO result = twoFactorAuthService.initializeSetup(user);

        ArgumentCaptor<String> secretCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisCacheService).set(eq(RedisKeyConstants.TWO_FACTOR_SETUP_PREFIX + 1L), secretCaptor.capture(), eq(Duration.ofMinutes(10)));
        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getManualEntryKey()).isEqualTo(secretCaptor.getValue());
        assertThat(result.getOtpAuthUri()).contains("otpauth://totp/");
        assertThat(result.getOtpAuthUri()).contains("secret=" + secretCaptor.getValue());
    }

    @Test
    void initializeSetupShouldDeriveIssuerFromFrontendBaseUrlWhenIssuerNotConfigured() {
        User user = user(5L, "erin@example.com");
        ReflectionTestUtils.setField(twoFactorAuthService, "issuer", "");
        ReflectionTestUtils.setField(twoFactorAuthService, "frontendBaseUrl", "https://123.45.67.89:9002/login");

        TwoFactorSetupVO result = twoFactorAuthService.initializeSetup(user);

        assertThat(result.getOtpAuthUri()).contains("otpauth://totp/123.45.67.89:erin%40example.com");
        assertThat(result.getOtpAuthUri()).contains("issuer=123.45.67.89");
    }

    /**
     * This test proves a real TOTP code can be verified by freezing Instant.now
     * entirely from the test side, without changing service code.
     */
    @Test
    void enableShouldAcceptValidCodeAtFixedInstant() {
        User user = user(2L, "bob@example.com");
        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String code = generateCodeForInstant(FIXED_SECRET, fixedInstant);
        String setupKey = RedisKeyConstants.TWO_FACTOR_SETUP_PREFIX + 2L;

        when(redisCacheService.get(setupKey, String.class)).thenReturn(Optional.of(FIXED_SECRET));

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            twoFactorAuthService.enable(user, code);
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(redisCacheService).delete(setupKey);
        assertThat(userCaptor.getValue().getTwoFactorEnabled()).isTrue();
        assertThat(userCaptor.getValue().getTwoFactorSecret()).isEqualTo(FIXED_SECRET);
        assertThat(userCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    /** Rejects setup enablement when the provided TOTP code is invalid. */
    @Test
    void enableShouldRejectInvalidCodeAtFixedInstant() {
        User user = user(3L, "carol@example.com");
        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String setupKey = RedisKeyConstants.TWO_FACTOR_SETUP_PREFIX + 3L;

        when(redisCacheService.get(setupKey, String.class)).thenReturn(Optional.of(FIXED_SECRET));

        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> twoFactorAuthService.enable(user, "000000"));

            assertThat(exception.getMessage()).isEqualTo(MessageConstants.TWO_FACTOR_CODE_INVALID);
        }
    }

    /**
     * Login challenge verification uses the same TOTP validator. Freezing the
     * clock here exercises the real challenge + secret + code path end to end.
     */
    @Test
    void verifyLoginChallengeShouldAcceptValidCodeAtFixedInstant() {
        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String token = "challenge-token";
        String challengeKey = RedisKeyConstants.TWO_FACTOR_LOGIN_CHALLENGE_PREFIX + token;
        String code = generateCodeForInstant(FIXED_SECRET, fixedInstant);
        User user = user(4L, "dave@example.com");
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(FIXED_SECRET);

        when(redisCacheService.get(challengeKey, TwoFactorLoginChallenge.class))
                .thenReturn(Optional.of(new TwoFactorLoginChallenge(4L)));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));

        User verifiedUser;
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);
            verifiedUser = twoFactorAuthService.verifyLoginChallenge(token, code);
        }

        verify(redisCacheService).delete(challengeKey);
        assertThat(verifiedUser).isSameAs(user);
    }

    private User user(Long userId, String email) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setTwoFactorEnabled(false);
        return user;
    }

    private String generateCodeForInstant(String secret, Instant instant) {
        long counter = instant.getEpochSecond() / 30;
        return ReflectionTestUtils.invokeMethod(TotpUtil.class, "generateCode", secret, counter);
    }
}