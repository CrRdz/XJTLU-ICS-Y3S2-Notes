package com.cpt202.validation;

import com.cpt202.model.entity.User;

/**
 * 用户资料领域约束验证服务。
 * 负责邮箱唯一性、角色匹配、密码一致性等资料相关约束。
 */
public interface ProfileValidationService {

    /**
     * 校验目标邮箱对该用户是否可用（格式正确且未被其他用户占用）。
     *
     * @param email  待校验的邮箱
     * @param userId 当前用户主键
     */
    void checkEmailAvailableForUser(String email, Long userId);

    /**
     * 泛型角色校验：校验用户是否具备指定角色。
     *
     * @param user         用户实体
     * @param expectedRole 期望的角色
     */
    void checkUserRole(User user, User.UserRole expectedRole);

    /**
     * 校验用户是否为 STUDENT 角色。
     *
     * @param user 用户实体
     */
    void checkUserIsStudent(User user);

    /**
     * 校验用户是否为 TEACHER 角色。
     *
     * @param user 用户实体
     */
    void checkUserIsTeacher(User user);

    /**
     * 校验用户是否为 ADMIN 角色。
     *
     * @param user 用户实体
     */
    void checkUserIsAdmin(User user);

    /**
     * 校验旧密码是否与用户当前密码一致。
     *
     * @param user        用户实体
     * @param oldPassword 旧密码明文
     */
    void checkOldPasswordMatches(User user, String oldPassword);

    /**
     * 校验新密码是否与当前密码不同。
     *
     * @param user        用户实体
     * @param newPassword 新密码明文
     */
    void checkNewPasswordDiffers(User user, String newPassword);
}
