package com.cpt202.validation;

import com.cpt202.dto.RegisterUserDTO;
import com.cpt202.model.entity.User;

/**
 * 认证领域约束验证服务。
 * 负责邮箱格式、注册域名白名单、注册字段完整性、账号状态等认证相关约束。
 */
public interface AuthValidationService {

    /**
     * 校验邮箱格式是否符合基本规范。
     *
     * @param email 待校验的邮箱地址
     */
    void checkEmailFormat(String email);

    /**
     * 校验邮箱域名是否在可注册的白名单内。
     *
     * @param email 待校验的邮箱地址
     */
    void checkRegistrableEmailDomain(String email);

    /**
     * 根据邮箱域名推断用户角色。
     *
     * @param email 邮箱地址
     * @return 推断的角色
     */
    User.UserRole inferRoleFromEmail(String email);

    /**
     * 校验注册请求中各角色必填字段的完整性。
     *
     * @param dto  注册参数
     * @param role 目标角色
     */
    void checkRegisterPayload(RegisterUserDTO dto, User.UserRole role);

    /**
     * 校验用户账号是否为可用状态。
     *
     * @param user 用户实体
     */
    void checkAccountActive(User user);

    /**
     * 校验用户名是否已被占用。
     *
     * @param username 用户名
     */
    void checkUsernameUnique(String username);

    /**
     * 校验邮箱是否已被注册。
     *
     * @param email 邮箱地址
     */
    void checkEmailUnique(String email);
}
