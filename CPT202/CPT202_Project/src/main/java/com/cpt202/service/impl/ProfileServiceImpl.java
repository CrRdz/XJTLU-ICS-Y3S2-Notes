package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.util.PasswordUtil;
import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.dto.TwoFactorDisableDTO;
import com.cpt202.dto.TwoFactorEnableDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.ProfileService;
import com.cpt202.service.TwoFactorAuthService;
import com.cpt202.validation.ProfileValidationService;
import com.cpt202.vo.AdminProfileVO;
import com.cpt202.vo.StudentProfileVO;
import com.cpt202.vo.TeacherProfileVO;
import com.cpt202.vo.TwoFactorSetupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

/**
 * 用户资料服务实现类。
 * <p>
 * 使用泛型辅助方法消除学生/教师/管理员资料操作中的重复查找-校验模式。
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final ProfileValidationService profileValidationService;
    private final PasswordUtil passwordUtil;

    // ======================== 泛型辅助方法 ========================

    /**
     * 泛型查找 + 角色校验：根据 ID 查找实体，并校验其关联用户的角色。
     *
     * @param id             实体主键
     * @param repoFinder     仓库查找函数
     * @param notFoundMsg    未找到时的错误消息
     * @param userExtractor  从实体提取 User 的函数
     * @param requiredRole   要求的角色
     * @param <P>            实体类型 (StudentProfile / TeacherProfile / User)
     * @return 校验通过的实体
     */
    private <P> P requireProfile(
            Long id,
            Function<Long, Optional<P>> repoFinder,
            String notFoundMsg,
            Function<P, User> userExtractor,
            User.UserRole requiredRole
    ) {
        P profile = repoFinder.apply(id)
                .orElseThrow(() -> new NotFoundException(notFoundMsg));
        profileValidationService.checkUserRole(userExtractor.apply(profile), requiredRole);
        return profile;
    }

    /**
     * 将 User 的公共字段填充到任意 Profile VO 中。
     * 通过反射调用 setter，兼容 StudentProfileVO / TeacherProfileVO / AdminProfileVO。
     */
    private void fillUserFieldsToVO(User user, Object vo) {
        try {
            vo.getClass().getMethod("setUsername", String.class).invoke(vo, user.getUsername());
            vo.getClass().getMethod("setEmail", String.class).invoke(vo, user.getEmail());
            vo.getClass().getMethod("setFullName", String.class).invoke(vo, user.getFullName());
            vo.getClass().getMethod("setTwoFactorEnabled", Boolean.class)
                    .invoke(vo, Boolean.TRUE.equals(user.getTwoFactorEnabled()));
        } catch (Exception ignored) {
            // VO 类型可能没有全部字段（防御性忽略）
        }
    }

    // ======================== 学生资料 ========================

    /**
     * 查询学生资料。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    @Override
    public StudentProfileVO getStudentProfile(Long studentId) {
        StudentProfile profile = requireProfile(
                studentId, studentProfileRepository::findById,
                MessageConstants.STUDENT_PROFILE_NOT_FOUND,
                StudentProfile::getUser, User.UserRole.STUDENT);

        StudentProfileVO profileVO = new StudentProfileVO();
        BeanUtils.copyProperties(profile, profileVO);
        fillUserFieldsToVO(profile.getUser(), profileVO);
        profileVO.setAcademicYear(profile.getAcademicYear());
        return profileVO;
    }

    /**
     * 修改学生资料。
     *
     * @param studentId 学生主键
     * @param studentProfileUpdateDTO 学生资料更新参数
     */
    @Override
    @Transactional
    public void updateStudentProfile(Long studentId, StudentProfileUpdateDTO studentProfileUpdateDTO) {
        StudentProfile profile = requireProfile(
                studentId, studentProfileRepository::findById,
                MessageConstants.STUDENT_PROFILE_NOT_FOUND,
                StudentProfile::getUser, User.UserRole.STUDENT);

        profileValidationService.checkEmailAvailableForUser(studentProfileUpdateDTO.getEmail(), profile.getUser().getUserId());
        BeanUtils.copyProperties(studentProfileUpdateDTO, profile, "fullName", "email");
        profile.getUser().setFullName(studentProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(studentProfileUpdateDTO.getEmail().trim());
        profile.getUser().setUpdatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        studentProfileRepository.save(profile);
    }

    // ======================== 教师资料 ========================

    /**
     * 查询教师资料。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    @Override
    public TeacherProfileVO getTeacherProfile(Long teacherId) {
        TeacherProfile profile = requireProfile(
                teacherId, teacherProfileRepository::findById,
                MessageConstants.TEACHER_PROFILE_NOT_FOUND,
                TeacherProfile::getUser, User.UserRole.TEACHER);

        TeacherProfileVO profileVO = new TeacherProfileVO();
        BeanUtils.copyProperties(profile, profileVO);
        fillUserFieldsToVO(profile.getUser(), profileVO);
        return profileVO;
    }

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     */
    @Override
    @Transactional
    public void updateTeacherProfile(Long teacherId, TeacherProfileUpdateDTO teacherProfileUpdateDTO) {
        TeacherProfile profile = requireProfile(
                teacherId, teacherProfileRepository::findById,
                MessageConstants.TEACHER_PROFILE_NOT_FOUND,
                TeacherProfile::getUser, User.UserRole.TEACHER);

        profileValidationService.checkEmailAvailableForUser(teacherProfileUpdateDTO.getEmail(), profile.getUser().getUserId());
        BeanUtils.copyProperties(teacherProfileUpdateDTO, profile, "fullName", "email");
        profile.getUser().setFullName(teacherProfileUpdateDTO.getFullName());
        profile.getUser().setEmail(teacherProfileUpdateDTO.getEmail().trim());
        profile.getUser().setUpdatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        teacherProfileRepository.save(profile);
    }

    // ======================== 管理员资料 ========================

    @Override
    public AdminProfileVO getAdminProfile(Long userId) {
        User user = requireProfile(
                userId, userRepository::findById,
                MessageConstants.USER_NOT_FOUND,
                Function.identity(), User.UserRole.ADMIN);

        AdminProfileVO profileVO = new AdminProfileVO();
        BeanUtils.copyProperties(user, profileVO);
        profileVO.setTwoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()));
        return profileVO;
    }

    @Override
    @Transactional
    public void updateAdminProfile(Long userId, AdminProfileUpdateDTO adminProfileUpdateDTO) {
        User user = requireProfile(
                userId, userRepository::findById,
                MessageConstants.USER_NOT_FOUND,
                Function.identity(), User.UserRole.ADMIN);

        profileValidationService.checkEmailAvailableForUser(adminProfileUpdateDTO.getEmail(), user.getUserId());
        user.setFullName(adminProfileUpdateDTO.getFullName());
        user.setEmail(adminProfileUpdateDTO.getEmail().trim());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Change the password for the given user.
     * Verifies the old password before applying the new hash.
     *
     * @param userId            current user's primary key
     * @param changePasswordDTO old and new password payload
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        User user = getUserRequired(userId);

        profileValidationService.checkOldPasswordMatches(user, changePasswordDTO.getOldPassword());
        profileValidationService.checkNewPasswordDiffers(user, changePasswordDTO.getNewPassword());

        String newHash = passwordUtil.hash(changePasswordDTO.getNewPassword());
        user.setPasswordHash(newHash);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public TwoFactorSetupVO initializeTwoFactorSetup(Long userId) {
        return twoFactorAuthService.initializeSetup(getUserRequired(userId));
    }

    @Override
    @Transactional
    public void enableTwoFactor(Long userId, TwoFactorEnableDTO twoFactorEnableDTO) {
        String code = twoFactorEnableDTO.getCode() == null ? "" : twoFactorEnableDTO.getCode().trim();
        if (code.isEmpty()) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_CODE_REQUIRED);
        }
        twoFactorAuthService.enable(getUserRequired(userId), code);
    }

    @Override
    @Transactional
    public void disableTwoFactor(Long userId, TwoFactorDisableDTO twoFactorDisableDTO) {
        User user = getUserRequired(userId);
        String currentPassword = twoFactorDisableDTO.getCurrentPassword() == null ? "" : twoFactorDisableDTO.getCurrentPassword().trim();
        if (currentPassword.isEmpty()) {
            throw new BusinessException(MessageConstants.TWO_FACTOR_DISABLE_PASSWORD_REQUIRED);
        }
        String currentHash = passwordUtil.hash(currentPassword);
        if (!currentHash.equals(user.getPasswordHash())) {
            throw new BusinessException(MessageConstants.INCORRECT_OLD_PASSWORD);
        }
        twoFactorAuthService.disable(user);
    }

    private User getUserRequired(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));
    }

    // --- Validation logic moved to ProfileValidationService ---
}
