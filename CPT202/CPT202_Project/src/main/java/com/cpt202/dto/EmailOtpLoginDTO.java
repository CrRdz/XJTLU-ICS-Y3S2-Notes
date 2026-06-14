package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮箱验证码登录参数。
 */
@Data
public class EmailOtpLoginDTO {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String otp;
}
