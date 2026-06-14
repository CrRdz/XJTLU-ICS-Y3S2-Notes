package com.cpt202.acceptance;

import com.cpt202.model.entity.User;
import com.cpt202.integration.IntegrationTestSupport;
import com.cpt202.service.EmailOtpMailService;
import com.cpt202.util.TotpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Acceptance stories for public authentication. */
@Cpt202AcceptanceTest
class CommonAuthAcceptanceTest extends IntegrationTestSupport {

    private static final String FIXED_SECRET = "JBSWY3DPEHPK3PXP";

    @MockBean
    private EmailOtpMailService emailOtpMailService;

    private String emailedOtp;
    private String emailedRegisterOtp;

    /** Captures the OTP value emitted by the mocked mail sender. */
    @BeforeEach
    void setUpMailCapture() {
        emailedOtp = null;
        emailedRegisterOtp = null;
        doAnswer(invocation -> {
            emailedOtp = invocation.getArgument(1, String.class);
            return null;
        }).when(emailOtpMailService).sendLoginOtpMail(any(User.class), anyString());
        doAnswer(invocation -> {
            emailedRegisterOtp = invocation.getArgument(1, String.class);
            return null;
        }).when(emailOtpMailService).sendRegisterOtpMail(anyString(), anyString());
    }

    /** Visitor registers as a student and receives an authenticated response. */
    @Test
    void visitorCanRegisterAsStudent() throws Exception {
        String suffix = uniqueSuffix();
        String email = "acceptance-student-" + suffix + "@student.xjtlu.edu.cn";

        mockMvc.perform(post("/api/common/auth/register/email-otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(emailedRegisterOtp).matches("\\d{6}");

        MvcResult registerResult = mockMvc.perform(post("/api/common/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("username", "acceptance-student-" + suffix),
                                Map.entry("password", DEFAULT_PASSWORD),
                                Map.entry("email", email),
                                Map.entry("otp", emailedRegisterOtp),
                                Map.entry("fullName", "Acceptance Student " + suffix),
                                Map.entry("studentNo", "AS" + suffix),
                                Map.entry("programme", "Software Engineering"),
                                Map.entry("enrollmentDate", "2024-09-01"),
                                Map.entry("phone", "18800001111"),
                                Map.entry("interests", "Acceptance Testing")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.role").value("STUDENT"))
                .andExpect(jsonPath("$.data.username").value("acceptance-student-" + suffix))
                .andExpect(jsonPath("$.data.twoFactorRequired").value(false))
                .andReturn();

        assertThat(readData(registerResult).path("token").asText()).isNotBlank();
    }

    /** Existing user requests an email OTP and completes login with it. */
    @Test
    void existingUserCanLoginWithEmailOtp() throws Exception {
        String suffix = uniqueSuffix();
        User user = createUser(
                "acceptance-otp-" + suffix,
                DEFAULT_PASSWORD,
                "acceptance-otp-" + suffix + "@example.com",
                "Acceptance Otp " + suffix,
                User.UserRole.STUDENT
        );

        mockMvc.perform(post("/api/common/auth/email-otp/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", user.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(emailedOtp).matches("\\d{6}");

        MvcResult loginResult = mockMvc.perform(post("/api/common/auth/email-otp/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "otp", emailedOtp
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.twoFactorRequired").value(false))
                .andReturn();

        assertThat(readData(loginResult).path("token").asText()).isNotBlank();
    }

    /** Two-factor user completes password login by verifying a TOTP challenge. */
    @Test
    void twoFactorUserCanCompletePasswordLogin() throws Exception {
        String suffix = uniqueSuffix();
        User user = createUser(
                "acceptance-2fa-" + suffix,
                DEFAULT_PASSWORD,
                "acceptance-2fa-" + suffix + "@example.com",
                "Acceptance Two Factor " + suffix,
                User.UserRole.ADMIN
        );
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(FIXED_SECRET);
        userRepository.save(user);

        Instant fixedInstant = Instant.parse("2026-05-04T12:00:00Z");
        String code = generateCodeForInstant(FIXED_SECRET, fixedInstant);

        MvcResult verifyResult;
        try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS)) {
            mockedInstant.when(Instant::now).thenReturn(fixedInstant);

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

            String challengeToken = readData(loginResult).path("twoFactorChallengeToken").asText();
            assertThat(challengeToken).isNotBlank();

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

        assertThat(readData(verifyResult).path("token").asText()).isNotBlank();
    }

    /** Reads the data payload from a wrapped API response. */
    private JsonNode readData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    /** Generates a deterministic TOTP code for a fixed instant. */
    private String generateCodeForInstant(String secret, Instant instant) {
        long counter = instant.getEpochSecond() / 30;
        return ReflectionTestUtils.invokeMethod(TotpUtil.class, "generateCode", secret, counter);
    }
}