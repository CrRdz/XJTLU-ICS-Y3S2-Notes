package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员更新用户基础信息参数。
 */
@Data
public class AdminUserUpdateDTO {

    /** 登录用户名。 */
    @NotBlank
    private String username;

    /** 用户邮箱。 */
    @Email
    @NotBlank
    private String email;

    /** 用户真实姓名。 */
    @NotBlank
    private String fullName;
}
