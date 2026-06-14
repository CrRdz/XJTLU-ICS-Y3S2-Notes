package com.cpt202.integration;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.model.entity.User;
import com.cpt202.security.TwoFactorLoginChallenge;
import com.cpt202.service.EmailOtpMailService;
import com.cpt202.service.RedisCacheService;
import com.cpt202.util.TotpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Interface-level integration tests for the public authentication controller.
 *
 * The controller and service beans stay real. Only Redis-backed transient auth
 * state is replaced with a deterministic in-memory store so email OTP and 2FA
 * challenge flows can be executed end to end through HTTP.
 */
@Cpt202IntegrationTest
class CommonAuthControllerIntegrationTest extends IntegrationTestSupport {

    private static final String FIXED_SECRET = "JBSWY3DPEHPK3PXP";

    @MockBean
    private RedisCacheService redisCacheService;

    @MockBean
    private EmailOtpMailService emailOtpMailService;

    private ConcurrentMap<String, Object> redisEntries;

    /** Installs a deterministic in-memory Redis stub for transient auth state. */
    @BeforeEach
    void setUpRedisStub() {
        redisEntries = new ConcurrentHashMap<>();

        doAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            Object value = invocation.getArgument(1);
            redisEntries.put(key, value);
            return null;
        }).when(redisCacheService).set(anyString(), any(), any(Duration.class));

        doAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            redisEntries.remove(key);
            return null;
        }).when(redisCacheService).delete(anyString());

        doAnswer(invocation -> {
            String key = invocation.getArgument(0, String.class);
            Class<?> clazz = invocation.getArgument(1);
            Object value = redisEntries.get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(clazz.cast(value));
        }).when(redisCacheService).get(anyString(), ArgumentMatchers.<Class<?>>any());
    }

    /**
     * Full email-OTP flow:
     * 1. request an OTP through the public controller endpoint
     * 2. read the generated code from the in-memory Redis substitute
     * 3. submit the code back to the login endpoint
     * 4. assert the login completes and the one-time code is consumed
     */
    @Test
    void emailOtpEndpointsShouldSendOtpAndCompleteLogin() throws Exception {
        String suffix = uniqueSuffix();
        User user = createUser(
                "otp-user-" + suffix,
                DEFAULT_PASSWORD,
                "otp-user-" + suffix + "@example.com",
                "Otp User " + suffix,
                User.UserRole.STUDENT
        );

        mockMvc.perform(post("/api/common/auth/email-otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", user.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value(MessageConstants.EMAIL_OTP_SENT));

        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + user.getEmail().toLowerCase();
        Object storedOtp = redisEntries.get(otpKey);
        assertThat(storedOtp).isInstanceOf(String.class);
        assertThat((String) storedOtp).matches("\\d{6}");

        MvcResult loginResult = mockMvc.perform(post("/api/common/auth/email-otp/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "otp", storedOtp
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.twoFactorRequired").value(false))
                .andReturn();

        JsonNode loginData = readData(loginResult);
        assertThat(loginData.path("token").asText()).isNotBlank();
        assertThat(redisEntries).doesNotContainKey(otpKey);
    }

    /**
     * Full 2FA login flow:
     * 1. submit username/password to receive a challenge token
     * 2. verify that the transient challenge is cached
     * 3. freeze Instant.now so TOTP verification becomes deterministic
     * 4. submit the challenge token plus real TOTP code to finish login
     */
    @Test
    void twoFactorEndpointsShouldReturnChallengeAndCompleteLogin() throws Exception {
        String suffix = uniqueSuffix();
        User user = createUser(
                "twofactor-user-" + suffix,
                DEFAULT_PASSWORD,
                "twofactor-user-" + suffix + "@example.com",
                "Two Factor User " + suffix,
                User.UserRole.ADMIN
        );
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(FIXED_SECRET);
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/common/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", user.getUsername(),
                                "password", DEFAULT_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.twoFactorRequired").value(true))
                .andExpect(jsonPath("$.data.token").value(""))
                .andReturn();

        JsonNode loginData = readData(loginResult);
        String challengeToken = loginData.path("twoFactorChallengeToken").asText();
        assertThat(challengeToken).isNotBlank();

        String challengeKey = RedisKeyConstants.TWO_FACTOR_LOGIN_CHALLENGE_PREFIX + challengeToken;
        assertThat(redisEntries.get(challengeKey)).isInstanceOf(TwoFactorLoginChallenge.class);

        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String code = generateCodeForInstant(FIXED_SECRET, fixedInstant);

        MvcResult verifyResult;
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

            verifyResult = mockMvc.perform(post("/api/common/auth/2fa/verify-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "challengeToken", challengeToken,
                                    "code", code
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1))
                    .andExpect(jsonPath("$.data.userId").value(user.getUserId()))
                    .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                    .andExpect(jsonPath("$.data.twoFactorRequired").value(false))
                    .andReturn();
        }

        JsonNode verifyData = readData(verifyResult);
        assertThat(verifyData.path("token").asText()).isNotBlank();
        assertThat(redisEntries).doesNotContainKey(challengeKey);
    }

    /** Reads the wrapped data payload from a MockMvc response. */
    private JsonNode readData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    /** Generates a deterministic TOTP code for the supplied instant. */
    private String generateCodeForInstant(String secret, Instant instant) {
        long counter = instant.getEpochSecond() / 30;
        return ReflectionTestUtils.invokeMethod(TotpUtil.class, "generateCode", secret, counter);
    }
}