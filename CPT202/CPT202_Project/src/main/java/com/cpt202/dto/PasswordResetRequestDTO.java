package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Forgot-password email request payload.
 */
@Data
public class PasswordResetRequestDTO {

    @Email
    @NotBlank
    private String email;
}
