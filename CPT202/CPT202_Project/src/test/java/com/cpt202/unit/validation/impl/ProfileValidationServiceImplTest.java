package com.cpt202.unit.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.util.PasswordUtil;
import com.cpt202.validation.impl.ProfileValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Unit tests for profile-domain validation rules. */
@ExtendWith(MockitoExtension.class)
class ProfileValidationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileValidationServiceImpl profileValidationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileValidationService, "passwordUtil", new PasswordUtil());
    }

    private static String hash(String password) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static User activeUser(Long id, String role, String email, String passwordHash) {
        User u = new User();
        u.setUserId(id);
        u.setRole(User.UserRole.valueOf(role));
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        return u;
    }

    /* ---- checkEmailAvailableForUser ---- */

    @Test
    void checkEmailAvailableShouldPassForValidUniqueEmail() {
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("alice@example.com", 1L)).thenReturn(false);
        assertDoesNotThrow(() -> profileValidationService.checkEmailAvailableForUser("alice@example.com", 1L));
    }

    @Test
    void checkEmailAvailableShouldRejectInvalidFormat() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkEmailAvailableForUser("not-an-email", 1L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_FORMAT_INVALID);
    }

    @Test
    void checkEmailAvailableShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("dup@example.com", 1L)).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkEmailAvailableForUser("dup@example.com", 1L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_EXISTS);
    }

    @Test
    void checkEmailAvailableShouldTrimAndRejectDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCaseAndUserIdNot("dup@example.com", 1L)).thenReturn(true);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkEmailAvailableForUser(" dup@example.com ", 1L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_EXISTS);
    }

    /* ---- checkUserIsStudent ---- */

    @Test
    void checkUserIsStudentShouldPass() {
        assertDoesNotThrow(() -> profileValidationService.checkUserIsStudent(
                activeUser(1L, "STUDENT", "s@x.com", hash("pw"))));
    }

    @Test
    void checkUserIsStudentShouldRejectTeacher() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkUserIsStudent(
                        activeUser(2L, "TEACHER", "t@x.com", hash("pw"))));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.NON_STUDENT_PROFILE_ACCESS);
    }

    @Test
    void checkUserIsStudentShouldRejectNull() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkUserIsStudent(null));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.NON_STUDENT_PROFILE_ACCESS);
    }

    /* ---- checkUserIsTeacher ---- */

    @Test
    void checkUserIsTeacherShouldPass() {
        assertDoesNotThrow(() -> profileValidationService.checkUserIsTeacher(
                activeUser(1L, "TEACHER", "t@x.com", hash("pw"))));
    }

    @Test
    void checkUserIsTeacherShouldRejectStudent() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkUserIsTeacher(
                        activeUser(2L, "STUDENT", "s@x.com", hash("pw"))));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.NON_TEACHER_PROFILE_ACCESS);
    }

    /* ---- checkUserIsAdmin ---- */

    @Test
    void checkUserIsAdminShouldPass() {
        assertDoesNotThrow(() -> profileValidationService.checkUserIsAdmin(
                activeUser(1L, "ADMIN", "a@x.com", hash("pw"))));
    }

    @Test
    void checkUserIsAdminShouldRejectTeacher() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkUserIsAdmin(
                        activeUser(2L, "TEACHER", "t@x.com", hash("pw"))));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.NON_ADMIN_PROFILE_ACCESS);
    }

    /* ---- checkOldPasswordMatches ---- */

    @Test
    void checkOldPasswordMatchesShouldPass() {
        User user = activeUser(1L, "STUDENT", "s@x.com", hash("CorrectPass"));
        assertDoesNotThrow(() -> profileValidationService.checkOldPasswordMatches(user, "CorrectPass"));
    }

    @Test
    void checkOldPasswordMatchesShouldRejectWrongPassword() {
        User user = activeUser(1L, "STUDENT", "s@x.com", hash("CorrectPass"));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkOldPasswordMatches(user, "WrongPass"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.INCORRECT_OLD_PASSWORD);
    }

    /* ---- checkNewPasswordDiffers ---- */

    @Test
    void checkNewPasswordDiffersShouldPass() {
        User user = activeUser(1L, "STUDENT", "s@x.com", hash("OldPass"));
        assertDoesNotThrow(() -> profileValidationService.checkNewPasswordDiffers(user, "NewPass"));
    }

    @Test
    void checkNewPasswordDiffersShouldRejectSamePassword() {
        User user = activeUser(1L, "STUDENT", "s@x.com", hash("SamePass"));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> profileValidationService.checkNewPasswordDiffers(user, "SamePass"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.NEW_PASSWORD_SAME_AS_OLD);
    }
}
