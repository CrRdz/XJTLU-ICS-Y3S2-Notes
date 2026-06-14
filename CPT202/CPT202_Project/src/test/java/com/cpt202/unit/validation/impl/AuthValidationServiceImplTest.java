package com.cpt202.unit.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.validation.impl.AuthValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Unit tests for auth-domain validation rules. */
@ExtendWith(MockitoExtension.class)
class AuthValidationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthValidationServiceImpl authValidationService;

    /* ---- checkEmailFormat ---- */

    @Test
    void checkEmailFormatShouldPassForValidEmail() {
        assertDoesNotThrow(() -> authValidationService.checkEmailFormat("alice@student.xjtlu.edu.cn"));
    }

    @Test
    void checkEmailFormatShouldRejectMissingAt() {
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkEmailFormat("not-an-email"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_FORMAT_INVALID);
    }

    @Test
    void checkEmailFormatShouldRejectMissingDomain() {
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkEmailFormat("user@"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_FORMAT_INVALID);
    }

    /* ---- checkRegistrableEmailDomain ---- */

    @Test
    void checkRegistrableEmailDomainShouldPassForStudentDomain() {
        assertDoesNotThrow(() -> authValidationService.checkRegistrableEmailDomain("alice@student.xjtlu.edu.cn"));
    }

    @Test
    void checkRegistrableEmailDomainShouldPassForTeacherDomain() {
        assertDoesNotThrow(() -> authValidationService.checkRegistrableEmailDomain("smith@xjtlu.edu.cn"));
    }

    @Test
    void checkRegistrableEmailDomainShouldRejectNonWhitelistDomain() {
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegistrableEmailDomain("user@gmail.com"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_DOMAIN_NOT_ALLOWED);
    }

    /* ---- inferRoleFromEmail ---- */

    @Test
    void inferRoleFromEmailShouldReturnStudent() {
        assertThat(authValidationService.inferRoleFromEmail("alice@student.xjtlu.edu.cn"))
                .isEqualTo(User.UserRole.STUDENT);
    }

    @Test
    void inferRoleFromEmailShouldReturnTeacher() {
        assertThat(authValidationService.inferRoleFromEmail("smith@xjtlu.edu.cn"))
                .isEqualTo(User.UserRole.TEACHER);
    }

    @Test
    void inferRoleFromEmailShouldRejectNonWhitelistDomain() {
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.inferRoleFromEmail("user@gmail.com"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_DOMAIN_NOT_ALLOWED);
    }

    /* ---- checkRegisterPayload (STUDENT) ---- */

    @Test
    void checkRegisterPayloadShouldPassForValidStudent() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStudentNo("S2024001");
        dto.setProgramme("Software Engineering");
        dto.setEnrollmentDate(LocalDate.now().minusYears(1));

        assertDoesNotThrow(() -> authValidationService.checkRegisterPayload(dto, User.UserRole.STUDENT));
    }

    @Test
    void checkRegisterPayloadShouldRejectMissingStudentNo() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStudentNo("");
        dto.setProgramme("SE");
        dto.setEnrollmentDate(LocalDate.now());

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.STUDENT));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.STUDENT_NO_REQUIRED);
    }

    @Test
    void checkRegisterPayloadShouldRejectMissingProgramme() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStudentNo("S2024001");
        dto.setProgramme("");
        dto.setEnrollmentDate(LocalDate.now());

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.STUDENT));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.STUDENT_PROGRAMME_REQUIRED);
    }

    @Test
    void checkRegisterPayloadShouldRejectNullEnrollmentDate() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStudentNo("S2024001");
        dto.setProgramme("SE");

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.STUDENT));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.STUDENT_ENROLLMENT_DATE_REQUIRED);
    }

    @Test
    void checkRegisterPayloadShouldRejectFutureEnrollmentDate() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStudentNo("S2024001");
        dto.setProgramme("SE");
        dto.setEnrollmentDate(LocalDate.now().plusDays(1));

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.STUDENT));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.ENROLLMENT_DATE_CANNOT_BE_FUTURE);
    }

    /* ---- checkRegisterPayload (TEACHER) ---- */

    @Test
    void checkRegisterPayloadShouldPassForValidTeacher() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStaffNo("T2024001");
        dto.setDepartment("Computer Science");
        dto.setTitle("Associate Professor");

        assertDoesNotThrow(() -> authValidationService.checkRegisterPayload(dto, User.UserRole.TEACHER));
    }

    @Test
    void checkRegisterPayloadShouldRejectMissingStaffNo() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStaffNo("");
        dto.setDepartment("CS");
        dto.setTitle("Professor");

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.TEACHER));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.TEACHER_STAFF_NO_REQUIRED);
    }

    @Test
    void checkRegisterPayloadShouldRejectMissingDepartment() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStaffNo("T2024001");
        dto.setDepartment("");
        dto.setTitle("Professor");

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.TEACHER));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.TEACHER_DEPARTMENT_REQUIRED);
    }

    @Test
    void checkRegisterPayloadShouldRejectMissingTitle() {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setStaffNo("T2024001");
        dto.setDepartment("CS");
        dto.setTitle("");

        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkRegisterPayload(dto, User.UserRole.TEACHER));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.TEACHER_TITLE_REQUIRED);
    }

    /* ---- checkAccountActive ---- */

    @Test
    void checkAccountActiveShouldPassForActiveUser() {
        User user = new User();
        user.setAccountStatus("ACTIVE");
        assertDoesNotThrow(() -> authValidationService.checkAccountActive(user));
    }

    @Test
    void checkAccountActiveShouldRejectDisabledUser() {
        User user = new User();
        user.setAccountStatus("DISABLED");
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkAccountActive(user));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
    }

    @Test
    void checkAccountActiveShouldRejectNullUser() {
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkAccountActive(null));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
    }

    /* ---- checkUsernameUnique ---- */

    @Test
    void checkUsernameUniqueShouldPassWhenNotExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        assertDoesNotThrow(() -> authValidationService.checkUsernameUnique("alice"));
    }

    @Test
    void checkUsernameUniqueShouldRejectWhenExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkUsernameUnique("alice"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.USERNAME_EXISTS);
    }

    /* ---- checkEmailUnique ---- */

    @Test
    void checkEmailUniqueShouldPassWhenNotExists() {
        when(userRepository.existsByEmail("alice@xjtlu.edu.cn")).thenReturn(false);
        assertDoesNotThrow(() -> authValidationService.checkEmailUnique("alice@xjtlu.edu.cn"));
    }

    @Test
    void checkEmailUniqueShouldRejectWhenExists() {
        when(userRepository.existsByEmail("alice@xjtlu.edu.cn")).thenReturn(true);
        RuleViolationException ex = assertThrows(RuleViolationException.class,
                () -> authValidationService.checkEmailUnique("alice@xjtlu.edu.cn"));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.EMAIL_EXISTS);
    }
}
