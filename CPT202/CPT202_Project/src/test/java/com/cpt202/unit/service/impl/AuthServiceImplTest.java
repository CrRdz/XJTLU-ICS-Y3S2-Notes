package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.dto.EmailOtpLoginDTO;
import com.cpt202.dto.EmailOtpRequestDTO;
import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.PasswordResetTokenRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.*;
import com.cpt202.service.impl.AuthServiceImpl;
import com.cpt202.util.PasswordUtil;
import com.cpt202.validation.AuthValidationService;
import com.cpt202.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for authentication, registration, OTP, and 2FA service rules. */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @Mock
    private EmailOtpMailService emailLoginOtpMailService;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private AuthValidationService authValidationService;

    @InjectMocks
    private AuthServiceImpl authService;

    /** Configures OTP timing properties used by the service under test. */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "passwordUtil", new PasswordUtil());
        ReflectionTestUtils.setField(authService, "emailLoginOtpExpirationMinutes", 5L);
        ReflectionTestUtils.setField(authService, "emailLoginOtpCooldownSeconds", 60L);
    }

    /** Rejects student registration when the enrollment date is in the future. */
    @Test
    void registerShouldRejectFutureEnrollmentDate() {
        RegisterUserDTO dto = validStudentRegisterDTO();
        dto.setEnrollmentDate(LocalDate.now().plusDays(1));// Set enrollment date to tomorrow

        when(authValidationService.inferRoleFromEmail(dto.getEmail().trim())).thenReturn(User.UserRole.STUDENT);
        doThrow(new RuleViolationException(MessageConstants.ENROLLMENT_DATE_CANNOT_BE_FUTURE))
                .when(authValidationService).checkRegisterPayload(any(RegisterUserDTO.class), eq(User.UserRole.STUDENT));

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> authService.register(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.ENROLLMENT_DATE_CANNOT_BE_FUTURE);
    }

    /** Rejects registration when the requested username already exists. */
    @Test
    void registerShouldRejectDuplicateUsername() {
        RegisterUserDTO dto = validStudentRegisterDTO();
        dto.setOtp("123456");
        String otpKey = RedisKeyConstants.EMAIL_REGISTER_OTP_PREFIX + dto.getEmail().trim().toLowerCase();
        when(redisCacheService.get(otpKey, String.class)).thenReturn(Optional.of("123456"));

        when(authValidationService.inferRoleFromEmail(dto.getEmail().trim())).thenReturn(User.UserRole.STUDENT);
        doThrow(new RuleViolationException(MessageConstants.USERNAME_EXISTS))
                .when(authValidationService).checkUsernameUnique(dto.getUsername());

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> authService.register(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.USERNAME_EXISTS);
        verify(userRepository, never()).save(any(User.class));
    }

    /** Creates a student profile and returns a login payload after registration. */
    @Test
    void registerStudentShouldCreateProfileAndReturnLoginVo() {
        RegisterUserDTO dto = validStudentRegisterDTO();
        dto.setOtp("123456");
        String otpKey = RedisKeyConstants.EMAIL_REGISTER_OTP_PREFIX + dto.getEmail().trim().toLowerCase();
        when(redisCacheService.get(otpKey, String.class)).thenReturn(Optional.of("123456"));

        when(authValidationService.inferRoleFromEmail(dto.getEmail().trim())).thenReturn(User.UserRole.STUDENT);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(100L);
            return user;
        });
        when(studentProfileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.generateToken(any(User.class))).thenReturn("jwt-token");

        LoginVO result = authService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<StudentProfile> studentCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(userRepository).save(userCaptor.capture());
        verify(studentProfileRepository).save(studentCaptor.capture());

        User savedUser = userCaptor.getValue();
        StudentProfile savedStudent = studentCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(dto.getEmail().trim());
        assertThat(savedUser.getPasswordHash()).isEqualTo(hash(dto.getPassword()));
        assertThat(savedStudent.getStudentNo()).isEqualTo(dto.getStudentNo());
        assertThat(savedStudent.getUser()).isEqualTo(savedUser);
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getTwoFactorRequired()).isFalse();
    }

    /** Rejects password login when the password does not match. */
    @Test
    void loginShouldRejectInvalidPassword() {
        LoginDTO dto = loginDTO("alice", "wrong-password");
        User user = activeUser(1L, "alice", "alice@example.com", hash("CorrectPassword"));

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.INVALID_CREDENTIALS);
    }

    /** Returns a two-factor challenge instead of a token when 2FA is enabled. */
    @Test
    void loginShouldReturnTwoFactorChallengeWhenEnabled() {
        LoginDTO dto = loginDTO("alice", "CorrectPassword");
        User user = activeUser(2L, "alice", "alice@example.com", hash("CorrectPassword"));
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret("SECRET123");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(twoFactorAuthService.createLoginChallenge(user)).thenReturn("challenge-token");

        LoginVO result = authService.login(dto);

        assertThat(result.getToken()).isEmpty();
        assertThat(result.getTwoFactorRequired()).isTrue();
        assertThat(result.getTwoFactorChallengeToken()).isEqualTo("challenge-token");
    }

    /** Rejects OTP requests that arrive before the cooldown expires. */
    @Test
    void sendEmailLoginOtpShouldRejectFrequentRequests() {
        EmailOtpRequestDTO dto = otpRequestDTO("alice@example.com");
        String cooldownKey = RedisKeyConstants.EMAIL_LOGIN_OTP_COOLDOWN_PREFIX + "alice@example.com";

        when(redisCacheService.get(cooldownKey, String.class)).thenReturn(Optional.of("1"));

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> authService.sendEmailLoginOtp(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.EMAIL_OTP_REQUEST_TOO_FREQUENT);
        verify(emailLoginOtpMailService, never()).sendLoginOtpMail(any(User.class), anyString());
    }

    /** Stores an OTP and sends mail for an active user. */
    @Test
    void sendEmailLoginOtpShouldStoreOtpAndSendMailForActiveUser() {
        EmailOtpRequestDTO dto = otpRequestDTO("alice@example.com");
        String normalizedEmail = "alice@example.com";
        String cooldownKey = RedisKeyConstants.EMAIL_LOGIN_OTP_COOLDOWN_PREFIX + normalizedEmail;
        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + normalizedEmail;
        User user = activeUser(3L, "alice", normalizedEmail, hash("Password123"));

        when(redisCacheService.get(cooldownKey, String.class)).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase(normalizedEmail)).thenReturn(Optional.of(user));

        authService.sendEmailLoginOtp(dto);

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailLoginOtpMailService).sendLoginOtpMail(eq(user), otpCaptor.capture());
        verify(redisCacheService).set(eq(otpKey), eq(otpCaptor.getValue()), eq(Duration.ofMinutes(5)));
        verify(redisCacheService).set(eq(cooldownKey), eq("1"), eq(Duration.ofSeconds(60)));
        assertThat(otpCaptor.getValue()).matches("\\d{6}");
    }

    /** Rejects email-OTP login when the cached OTP has expired. */
    @Test
    void loginWithEmailOtpShouldRejectExpiredOtp() {
        EmailOtpLoginDTO dto = otpLoginDTO("alice@example.com", "123456");
        User user = activeUser(4L, "alice", "alice@example.com", hash("Password123"));
        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + "alice@example.com";

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(redisCacheService.get(otpKey, String.class)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.loginWithEmailOtp(dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.EMAIL_OTP_EXPIRED);
    }

    /** Deletes the OTP and returns a login payload after successful OTP login. */
    @Test
    void loginWithEmailOtpShouldDeleteOtpAndReturnLoginVoOnSuccess() {
        EmailOtpLoginDTO dto = otpLoginDTO("alice@example.com", "123456");
        User user = activeUser(5L, "alice", "alice@example.com", hash("Password123"));
        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + "alice@example.com";

        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(redisCacheService.get(otpKey, String.class)).thenReturn(Optional.of("123456"));
        when(jwtTokenService.generateToken(user)).thenReturn("otp-login-token");

        LoginVO result = authService.loginWithEmailOtp(dto);

        verify(redisCacheService).delete(otpKey);
        assertThat(result.getToken()).isEqualTo("otp-login-token");
        assertThat(result.getTwoFactorRequired()).isFalse();
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    private RegisterUserDTO validStudentRegisterDTO() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("student-user");
        dto.setPassword("Password123");
        dto.setEmail("student@student.xjtlu.edu.cn");
        dto.setFullName("Student User");
        dto.setStudentNo("S001");
        dto.setProgramme("Software Engineering");
        dto.setEnrollmentDate(LocalDate.now().minusYears(1));
        dto.setPhone("18800001111");
        dto.setInterests("Testing");
        return dto;
    }

    private RegisterUserDTO validTeacherRegisterDTO() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("teacher-user");
        dto.setPassword("Password123");
        dto.setEmail("teacher@xjtlu.edu.cn");
        dto.setFullName("Teacher User");
        dto.setStaffNo("123123");
        dto.setDepartment("EB123");
        dto.setTitle("Professor");
        dto.setPhone("18800001112");
        dto.setInterests("Testing");
        return dto;
    }


    private LoginDTO loginDTO(String username, String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    private EmailOtpRequestDTO otpRequestDTO(String email) {
        EmailOtpRequestDTO dto = new EmailOtpRequestDTO();
        dto.setEmail(email);
        return dto;
    }

    private EmailOtpLoginDTO otpLoginDTO(String email, String otp) {
        EmailOtpLoginDTO dto = new EmailOtpLoginDTO();
        dto.setEmail(email);
        dto.setOtp(otp);
        return dto;
    }

    private User activeUser(Long userId, String username, String email, String passwordHash) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setFullName("User " + userId);
        user.setRole(User.UserRole.STUDENT);
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return user;
    }

    private String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}