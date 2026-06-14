package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录参数。
 * <p>
 * 用于接收用户登录时提交的用户名和密码。
 */
@Data
public class LoginDTO {

    /** 登录用户名。 */
    @NotBlank
    private String username;

    /** 登录密码。 */
    @NotBlank
    private String password;
}
