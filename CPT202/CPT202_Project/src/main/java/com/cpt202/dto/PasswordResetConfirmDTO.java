package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Password reset confirmation payload.
 */
@Data
public class PasswordResetConfirmDTO {

    @NotBlank(message = "Reset token is required.")
    private String token;

    @NotBlank
    @Size(min = 6, message = "New password must be at least 6 characters.")
    private String newPassword;
}
