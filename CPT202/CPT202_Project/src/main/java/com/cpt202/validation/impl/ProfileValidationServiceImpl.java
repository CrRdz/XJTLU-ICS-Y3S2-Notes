package com.cpt202.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.util.PasswordUtil;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.validation.ProfileValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 用户资料领域约束验证服务实现。
 */
@Service
@RequiredArgsConstructor
public class ProfileValidationServiceImpl implements ProfileValidationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    @Override
    public void checkEmailAvailableForUser(String email, Long userId) {
        String trimmedEmail = email == null ? "" : email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new BusinessException(MessageConstants.EMAIL_FORMAT_INVALID);
        }
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(trimmedEmail, userId)) {
            throw new BusinessException(MessageConstants.EMAIL_EXISTS);
        }
    }

    @Override
    public void checkUserRole(User user, User.UserRole expectedRole) {
        if (user == null || user.getRole() != expectedRole) {
            throw new BusinessException(MessageConstants.ROLE_MISMATCH);
        }
    }

    @Override
    public void checkUserIsStudent(User user) {
        if (user == null || user.getRole() != User.UserRole.STUDENT) {
            throw new BusinessException(MessageConstants.NON_STUDENT_PROFILE_ACCESS);
        }
    }

    @Override
    public void checkUserIsTeacher(User user) {
        if (user == null || user.getRole() != User.UserRole.TEACHER) {
            throw new BusinessException(MessageConstants.NON_TEACHER_PROFILE_ACCESS);
        }
    }

    @Override
    public void checkUserIsAdmin(User user) {
        if (user == null || user.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException(MessageConstants.NON_ADMIN_PROFILE_ACCESS);
        }
    }

    @Override
    public void checkOldPasswordMatches(User user, String oldPassword) {
        if (!passwordUtil.hash(oldPassword).equals(user.getPasswordHash())) {
            throw new BusinessException(MessageConstants.INCORRECT_OLD_PASSWORD);
        }
    }

    @Override
    public void checkNewPasswordDiffers(User user, String newPassword) {
        if (passwordUtil.hash(newPassword).equals(user.getPasswordHash())) {
            throw new BusinessException(MessageConstants.NEW_PASSWORD_SAME_AS_OLD);
        }
    }

}
