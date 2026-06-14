package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.TwoFactorAuthService;
import com.cpt202.service.impl.ProfileServiceImpl;
import com.cpt202.util.PasswordUtil;
import com.cpt202.validation.ProfileValidationService;
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
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for profile updates and password-change validation rules. */
@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private ProfileValidationService profileValidationService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(profileService, "passwordUtil", new PasswordUtil());
    }

    /** Rejects student-profile updates when the profile is not owned by a student. */
    @Test
    void updateStudentProfileShouldRejectWhenRoleIsNotStudent() {
        User user = user(1L, User.UserRole.TEACHER, "teacher@example.com", hash("Password123"));
        StudentProfile profile = studentProfile(1L, user);

        when(studentProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        doThrow(new BusinessException(MessageConstants.NON_STUDENT_PROFILE_ACCESS))
                .when(profileValidationService).checkUserRole(any(User.class), eq(User.UserRole.STUDENT));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> profileService.updateStudentProfile(1L, studentProfileUpdateDTO("student@example.com")));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.NON_STUDENT_PROFILE_ACCESS);
        verify(studentProfileRepository, never()).save(any(StudentProfile.class));
    }

    /** Rejects admin-profile updates when the email format is invalid. */
    @Test
    void updateAdminProfileShouldRejectInvalidEmail() {
        User admin = user(2L, User.UserRole.ADMIN, "admin@example.com", hash("Password123"));
        AdminProfileUpdateDTO dto = new AdminProfileUpdateDTO();
        dto.setFullName("Admin Updated");
        dto.setEmail("not-an-email");

        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        doThrow(new BusinessException(MessageConstants.EMAIL_FORMAT_INVALID))
                .when(profileValidationService).checkEmailAvailableForUser(eq("not-an-email"), eq(2L));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> profileService.updateAdminProfile(2L, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.EMAIL_FORMAT_INVALID);
        verify(userRepository, never()).save(any(User.class));
    }

    /** Rejects teacher-profile updates when the email already exists elsewhere. */
    @Test
    void updateTeacherProfileShouldRejectDuplicatedEmail() {
        User teacherUser = user(3L, User.UserRole.TEACHER, "teacher@example.com", hash("Password123"));
        TeacherProfile profile = teacherProfile(3L, teacherUser);
        TeacherProfileUpdateDTO dto = new TeacherProfileUpdateDTO();
        dto.setFullName("Teacher Updated");
        dto.setEmail("dup@example.com");
        dto.setDepartment("CS");
        dto.setTitle("Professor");
        dto.setResearchArea("AI");
        dto.setOffice("A101");

        when(teacherProfileRepository.findById(3L)).thenReturn(Optional.of(profile));
        doThrow(new BusinessException(MessageConstants.EMAIL_EXISTS))
                .when(profileValidationService).checkEmailAvailableForUser(eq("dup@example.com"), eq(3L));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> profileService.updateTeacherProfile(3L, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.EMAIL_EXISTS);
        verify(teacherProfileRepository, never()).save(any(TeacherProfile.class));
    }

    /** Rejects password changes when the old password is incorrect. */
    @Test
    void changePasswordShouldRejectIncorrectOldPassword() {
        User user = user(4L, User.UserRole.ADMIN, "admin@example.com", hash("CorrectPass"));
        ChangePasswordDTO dto = changePasswordDTO("WrongPass", "NewPass123");

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        doThrow(new BusinessException(MessageConstants.INCORRECT_OLD_PASSWORD))
                .when(profileValidationService).checkOldPasswordMatches(any(User.class), eq("WrongPass"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> profileService.changePassword(4L, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.INCORRECT_OLD_PASSWORD);
        verify(userRepository, never()).save(any(User.class));
    }

    /** Rejects password changes that reuse the existing password. */
    @Test
    void changePasswordShouldRejectSamePassword() {
        User user = user(5L, User.UserRole.ADMIN, "admin@example.com", hash("SamePass123"));
        ChangePasswordDTO dto = changePasswordDTO("SamePass123", "SamePass123");

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        doThrow(new BusinessException(MessageConstants.NEW_PASSWORD_SAME_AS_OLD))
                .when(profileValidationService).checkNewPasswordDiffers(any(User.class), eq("SamePass123"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> profileService.changePassword(5L, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.NEW_PASSWORD_SAME_AS_OLD);
        verify(userRepository, never()).save(any(User.class));
    }

    /** Persists the new password hash after a valid password change. */
    @Test
    void changePasswordShouldPersistNewHash() {
        User user = user(6L, User.UserRole.ADMIN, "admin@example.com", hash("OldPass123"));
        ChangePasswordDTO dto = changePasswordDTO("OldPass123", "NewPass123");

        when(userRepository.findById(6L)).thenReturn(Optional.of(user));

        profileService.changePassword(6L, dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo(hash("NewPass123"));
        assertThat(userCaptor.getValue().getUpdatedAt()).isNotNull();
    }

    /** Trims and saves the student's email during profile updates. */
    @Test
    void updateStudentProfileShouldTrimEmailAndSave() {
        User user = user(7L, User.UserRole.STUDENT, "student@example.com", hash("Password123"));
        StudentProfile profile = studentProfile(7L, user);
        StudentProfileUpdateDTO dto = studentProfileUpdateDTO(" trimmed@example.com ");

        when(studentProfileRepository.findById(7L)).thenReturn(Optional.of(profile));

        profileService.updateStudentProfile(7L, dto);

        ArgumentCaptor<StudentProfile> profileCaptor = ArgumentCaptor.forClass(StudentProfile.class);
        verify(studentProfileRepository).save(profileCaptor.capture());
        StudentProfile savedProfile = profileCaptor.getValue();
        assertThat(savedProfile.getUser().getEmail()).isEqualTo("trimmed@example.com");
        assertThat(savedProfile.getUser().getFullName()).isEqualTo("Updated Student");
        assertThat(savedProfile.getProgramme()).isEqualTo("Software Engineering");
        assertThat(savedProfile.getUpdatedAt()).isNotNull();
    }

    private StudentProfileUpdateDTO studentProfileUpdateDTO(String email) {
        StudentProfileUpdateDTO dto = new StudentProfileUpdateDTO();
        dto.setFullName("Updated Student");
        dto.setEmail(email);
        dto.setProgramme("Software Engineering");
        dto.setPhone("18800001111");
        dto.setInterests("Testing");
        return dto;
    }

    private ChangePasswordDTO changePasswordDTO(String oldPassword, String newPassword) {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);
        return dto;
    }

    private User user(Long userId, User.UserRole role, String email, String passwordHash) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setFullName(role.name() + userId);
        user.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return user;
    }

    private StudentProfile studentProfile(Long studentId, User user) {
        StudentProfile studentProfile = new StudentProfile();
        ReflectionTestUtils.setField(studentProfile, "studentId", studentId);
        studentProfile.setUser(user);
        return studentProfile;
    }

    private TeacherProfile teacherProfile(Long teacherId, User user) {
        TeacherProfile teacherProfile = new TeacherProfile();
        ReflectionTestUtils.setField(teacherProfile, "teacherId", teacherId);
        teacherProfile.setUser(user);
        return teacherProfile;
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