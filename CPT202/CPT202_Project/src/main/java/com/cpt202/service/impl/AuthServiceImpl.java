package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.constant.RedisKeyConstants;
import com.cpt202.util.PasswordUtil;
import com.cpt202.dto.EmailOtpLoginDTO;
import com.cpt202.dto.EmailOtpRequestDTO;
import com.cpt202.dto.LoginDTO;
import com.cpt202.dto.PasswordResetConfirmDTO;
import com.cpt202.dto.PasswordResetRequestDTO;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.dto.TwoFactorLoginVerifyDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.PasswordResetToken;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.PasswordResetTokenRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.AuthService;
import com.cpt202.service.EmailOtpMailService;
import com.cpt202.service.JwtTokenService;
import com.cpt202.service.PasswordResetMailService;
import com.cpt202.service.RedisCacheService;
import com.cpt202.service.TwoFactorAuthService;
import com.cpt202.validation.AuthValidationService;
import com.cpt202.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Base64;

/**
 * 认证服务实现类。
 * <p>
 * 负责处理用户注册、登录、账号校验与登录态生成等逻辑。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ACCOUNT_STATUS = "ACTIVE";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordResetMailService passwordResetMailService;
    private final EmailOtpMailService emailOtpMailService;
    private final RedisCacheService redisCacheService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthValidationService authValidationService;
    private final PasswordUtil passwordUtil;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Value("${app.password-reset-token-expiration-minutes:30}")
    private long passwordResetTokenExpirationMinutes;

    @Value("${app.email-login-otp-expiration-minutes:5}")
    private long emailLoginOtpExpirationMinutes;

    @Value("${app.email-login-otp-cooldown-seconds:60}")
    private long emailLoginOtpCooldownSeconds;

    /**
     * 注册新用户。
     *
     * @param registerUserDTO 注册参数
     * @return 登录展示对象
     */
    @Override
    @Transactional
    public LoginVO register(RegisterUserDTO registerUserDTO) {
        String normalizedEmail = registerUserDTO.getEmail().trim().toLowerCase();
        authValidationService.checkEmailFormat(normalizedEmail);

        User.UserRole role = authValidationService.inferRoleFromEmail(normalizedEmail);

        authValidationService.checkRegisterPayload(registerUserDTO, role);

        // otp verification
        String submittedOtp = registerUserDTO.getOtp() == null ? "" : registerUserDTO.getOtp().trim();
        if (!StringUtils.hasText(submittedOtp)) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUIRED);
        }
        String otpKey = RedisKeyConstants.EMAIL_REGISTER_OTP_PREFIX + normalizedEmail;
        String storedOtp = redisCacheService.get(otpKey, String.class)
                .orElseThrow(() -> new BusinessException(MessageConstants.EMAIL_OTP_EXPIRED));
        if (!storedOtp.equals(submittedOtp)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_INVALID);
        }

        authValidationService.checkUsernameUnique(registerUserDTO.getUsername());
        authValidationService.checkEmailUnique(normalizedEmail);

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        BeanUtils.copyProperties(registerUserDTO, user, "password", "email", "otp");
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordUtil.hash(registerUserDTO.getPassword()));
        user.setRole(role);
        user.setAccountStatus(DEFAULT_ACCOUNT_STATUS);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        user = userRepository.save(user);

        // 根据角色创建对应的档案（泛型方法消除 if-else 重复）
        switch (user.getRole()) {
            case STUDENT -> user.setStudentProfile(
                    createRoleProfile(registerUserDTO, StudentProfile::new,
                            studentProfileRepository, user, now));
            case TEACHER -> user.setTeacherProfile(
                    createRoleProfile(registerUserDTO, TeacherProfile::new,
                            teacherProfileRepository, user, now));
            default -> { /* ADMIN — 无需创建角色档案 */ }
        }

        // otp removed
        redisCacheService.delete(otpKey);

        return buildLoginVO(user);
    }

    /**
     * 用户登录。
     *
     * @param loginDTO 登录参数
     * @return 登录展示对象
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(MessageConstants.INVALID_CREDENTIALS));

        if (!user.getPasswordHash().equals(passwordUtil.hash(loginDTO.getPassword()))) {
            throw new BusinessException(MessageConstants.INVALID_CREDENTIALS);
        }

        authValidationService.checkAccountActive(user);

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && StringUtils.hasText(user.getTwoFactorSecret())) {
            return buildTwoFactorRequiredLoginVO(user);
        }

        return buildLoginVO(user);
    }

    @Override
    public void sendEmailLoginOtp(EmailOtpRequestDTO requestDTO) {
        String normalizedEmail = requestDTO.getEmail().trim();
        authValidationService.checkEmailFormat(normalizedEmail);

        String cooldownKey = RedisKeyConstants.EMAIL_LOGIN_OTP_COOLDOWN_PREFIX + normalizedEmail.toLowerCase();
        if (redisCacheService.get(cooldownKey, String.class).isPresent()) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUEST_TOO_FREQUENT);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            return;
        }

        String otp = generateEmailOtp();
        log.info("[DEV] Login OTP for {} = {}", normalizedEmail, otp);
        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + normalizedEmail.toLowerCase();
        try {
            redisCacheService.set(otpKey, otp, Duration.ofMinutes(emailLoginOtpExpirationMinutes));
            redisCacheService.set(cooldownKey, "1", Duration.ofSeconds(emailLoginOtpCooldownSeconds));
            emailOtpMailService.sendLoginOtpMail(user, otp);
        } catch (RuntimeException ex) {
            log.error("Failed to send login OTP for email={}", normalizedEmail, ex);
            redisCacheService.delete(otpKey);
            redisCacheService.delete(cooldownKey);
            throw ex;
        }
    }

    @Override
    public void sendEmailRegisterOtp(EmailOtpRequestDTO requestDTO){
        String normalizedEmail = requestDTO.getEmail().trim().toLowerCase();
        authValidationService.checkEmailFormat(normalizedEmail);
        authValidationService.checkRegistrableEmailDomain(normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuleViolationException(MessageConstants.EMAIL_EXISTS);
        }

        String cooldownKey = RedisKeyConstants.EMAIL_REGISTER_OTP_COOLDOWN_PREFIX + normalizedEmail;
        if (redisCacheService.get(cooldownKey, String.class).isPresent()) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUEST_TOO_FREQUENT);
        }

        String otp = generateEmailOtp();
        log.info("[DEV] Register OTP for {} = {}", normalizedEmail, otp);
        String otpKey = RedisKeyConstants.EMAIL_REGISTER_OTP_PREFIX + normalizedEmail;

        try {
            redisCacheService.set(otpKey, otp, Duration.ofMinutes(emailLoginOtpExpirationMinutes));
            redisCacheService.set(cooldownKey, "1", Duration.ofSeconds(emailLoginOtpCooldownSeconds));
            emailOtpMailService.sendRegisterOtpMail(normalizedEmail, otp);
        } catch (RuntimeException ex) {
            log.error("Failed to send register OTP for email={}", normalizedEmail, ex);
            redisCacheService.delete(otpKey);
            redisCacheService.delete(cooldownKey);
            throw ex;
        }
    }



    @Override
    public LoginVO loginWithEmailOtp(EmailOtpLoginDTO loginDTO) {
        String normalizedEmail = loginDTO.getEmail().trim();
        authValidationService.checkEmailFormat(normalizedEmail);

        String submittedOtp = loginDTO.getOtp().trim();
        if (!StringUtils.hasText(submittedOtp)) {
            throw new RuleViolationException(MessageConstants.EMAIL_OTP_REQUIRED);
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BusinessException(MessageConstants.INVALID_CREDENTIALS));

        authValidationService.checkAccountActive(user);

        String otpKey = RedisKeyConstants.EMAIL_LOGIN_OTP_PREFIX + normalizedEmail.toLowerCase();
        String storedOtp = redisCacheService.get(otpKey, String.class)
                .orElseThrow(() -> new BusinessException(MessageConstants.EMAIL_OTP_EXPIRED));

        if (!storedOtp.equals(submittedOtp)) {
            throw new BusinessException(MessageConstants.EMAIL_OTP_INVALID);
        }

        redisCacheService.delete(otpKey);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO verifyPasswordLoginTwoFactor(TwoFactorLoginVerifyDTO verifyDTO) {
        String challengeToken = verifyDTO.getChallengeToken().trim();
        String code = verifyDTO.getCode().trim();
        if (!StringUtils.hasText(code)) {
            throw new RuleViolationException(MessageConstants.TWO_FACTOR_CODE_REQUIRED);
        }
        User user = twoFactorAuthService.verifyLoginChallenge(challengeToken, code);
        authValidationService.checkAccountActive(user);
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDTO requestDTO) {
        passwordResetTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        String normalizedEmail = requestDTO.getEmail().trim();
        authValidationService.checkEmailFormat(normalizedEmail);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            return;
        }

        invalidateActiveResetTokens(user, LocalDateTime.now());

        String rawToken = generateResetToken();
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(rawToken);
        passwordResetToken.setCreatedAt(now);
        passwordResetToken.setExpiresAt(now.plusMinutes(passwordResetTokenExpirationMinutes));
        passwordResetTokenRepository.save(passwordResetToken);

        passwordResetMailService.sendPasswordResetMail(user, buildResetLink(rawToken));
    }

    // --- Validation logic moved to AuthValidationService ---

    @Override
    @Transactional
    public void resetPassword(PasswordResetConfirmDTO confirmDTO) {
        if (!StringUtils.hasText(confirmDTO.getToken())) {
            throw new RuleViolationException(MessageConstants.PASSWORD_RESET_TOKEN_REQUIRED);
        }

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(confirmDTO.getToken().trim())
                .orElseThrow(() -> new BusinessException(MessageConstants.PASSWORD_RESET_LINK_INVALID));

        if (passwordResetToken.getUsedAt() != null) {
            throw new BusinessException(MessageConstants.PASSWORD_RESET_LINK_ALREADY_USED);
        }

        if (passwordResetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageConstants.PASSWORD_RESET_LINK_EXPIRED);
        }

        User user = passwordResetToken.getUser();
        user.setPasswordHash(passwordUtil.hash(confirmDTO.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        passwordResetToken.setUsedAt(LocalDateTime.now());
        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
        invalidateOtherActiveTokens(user, passwordResetToken.getResetId(), LocalDateTime.now());
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(jwtTokenService.generateToken(user));
        loginVO.setTwoFactorRequired(false);
        loginVO.setTwoFactorChallengeToken(null);
        return loginVO;
    }

    private LoginVO buildTwoFactorRequiredLoginVO(User user) {
        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken("");
        loginVO.setTwoFactorRequired(true);
        loginVO.setTwoFactorChallengeToken(twoFactorAuthService.createLoginChallenge(user));
        return loginVO;
    }

    private void invalidateActiveResetTokens(User user, LocalDateTime usedAt) {
        for (PasswordResetToken existingToken : passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)) {
            existingToken.setUsedAt(usedAt);
        }
    }

    private void invalidateOtherActiveTokens(User user, Long currentResetId, LocalDateTime usedAt) {
        for (PasswordResetToken existingToken : passwordResetTokenRepository.findAllByUserAndUsedAtIsNull(user)) {
            if (!existingToken.getResetId().equals(currentResetId)) {
                existingToken.setUsedAt(usedAt);
            }
        }
    }

    private String buildResetLink(String rawToken) {
        String baseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return "%s/auth/reset-password?token=%s".formatted(baseUrl, rawToken);
    }

    private String generateResetToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * 泛型角色档案创建：统一处理 StudentProfile / TeacherProfile 的初始化与持久化。
     * 通过反射设置 setUpdatedAt 与 setUser 以兼容不同档案类型。
     */
    private <P> P createRoleProfile(RegisterUserDTO dto,
                                     java.util.function.Supplier<P> factory,
                                     JpaRepository<P, Long> repo,
                                     User user, LocalDateTime now) {
        P profile = factory.get();
        BeanUtils.copyProperties(dto, profile);
        try {
            profile.getClass().getMethod("setUpdatedAt", LocalDateTime.class).invoke(profile, now);
            profile.getClass().getMethod("setUser", User.class).invoke(profile, user);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to initialize role profile fields", e);
        }
        return repo.save(profile);
    }

    private String generateEmailOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

}
