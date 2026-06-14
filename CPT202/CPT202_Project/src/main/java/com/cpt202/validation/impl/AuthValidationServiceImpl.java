package com.cpt202.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.validation.AuthValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * 认证领域约束验证服务实现。
 */
@Service
@RequiredArgsConstructor
public class AuthValidationServiceImpl implements AuthValidationService {

    private static final String DEFAULT_ACCOUNT_STATUS = "ACTIVE";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String STUDENT_EMAIL_DOMAIN = "student.xjtlu.edu.cn";
    private static final String TEACHER_EMAIL_DOMAIN = "xjtlu.edu.cn";

    private final UserRepository userRepository;

    @Override
    public void checkEmailFormat(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuleViolationException(MessageConstants.EMAIL_FORMAT_INVALID);
        }
    }

    @Override
    public void checkRegistrableEmailDomain(String email) {
        String domain = extractEmailDomain(email);
        if (!STUDENT_EMAIL_DOMAIN.equals(domain) && !TEACHER_EMAIL_DOMAIN.equals(domain)) {
            throw new RuleViolationException(MessageConstants.EMAIL_DOMAIN_NOT_ALLOWED);
        }
    }

    @Override
    public User.UserRole inferRoleFromEmail(String email) {
        String domain = extractEmailDomain(email);
        if (STUDENT_EMAIL_DOMAIN.equals(domain)) {
            return User.UserRole.STUDENT;
        }
        if (TEACHER_EMAIL_DOMAIN.equals(domain)) {
            return User.UserRole.TEACHER;
        }
        throw new RuleViolationException(MessageConstants.EMAIL_DOMAIN_NOT_ALLOWED);
    }

    @Override
    public void checkRegisterPayload(RegisterUserDTO dto, User.UserRole role) {
        if (role == User.UserRole.STUDENT) {
            if (!StringUtils.hasText(dto.getStudentNo())) {
                throw new RuleViolationException(MessageConstants.STUDENT_NO_REQUIRED);
            }
            if (!StringUtils.hasText(dto.getProgramme())) {
                throw new RuleViolationException(MessageConstants.STUDENT_PROGRAMME_REQUIRED);
            }
            if (dto.getEnrollmentDate() == null) {
                throw new RuleViolationException(MessageConstants.STUDENT_ENROLLMENT_DATE_REQUIRED);
            }
            if (dto.getEnrollmentDate().isAfter(LocalDate.now())) {
                throw new RuleViolationException(MessageConstants.ENROLLMENT_DATE_CANNOT_BE_FUTURE);
            }
        } else if (role == User.UserRole.TEACHER) {
            if (!StringUtils.hasText(dto.getStaffNo())) {
                throw new RuleViolationException(MessageConstants.TEACHER_STAFF_NO_REQUIRED);
            }
            if (!StringUtils.hasText(dto.getDepartment())) {
                throw new RuleViolationException(MessageConstants.TEACHER_DEPARTMENT_REQUIRED);
            }
            if (!StringUtils.hasText(dto.getTitle())) {
                throw new RuleViolationException(MessageConstants.TEACHER_TITLE_REQUIRED);
            }
        }
    }

    @Override
    public void checkAccountActive(User user) {
        if (user == null || !DEFAULT_ACCOUNT_STATUS.equalsIgnoreCase(user.getAccountStatus())) {
            throw new RuleViolationException(MessageConstants.ACCOUNT_UNAVAILABLE_CONTACT_ADMIN);
        }
    }

    @Override
    public void checkUsernameUnique(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new RuleViolationException(MessageConstants.USERNAME_EXISTS);
        }
    }

    @Override
    public void checkEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuleViolationException(MessageConstants.EMAIL_EXISTS);
        }
    }

    private String extractEmailDomain(String email) {
        int atIndex = email.indexOf('@');
        return atIndex < 0 ? "" : email.substring(atIndex + 1).toLowerCase();
    }
}
