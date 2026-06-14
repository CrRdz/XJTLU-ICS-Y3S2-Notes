package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮箱验证码请求参数。
 */
@Data
public class EmailOtpRequestDTO {

    @NotBlank
    @Email
    private String email;
}
