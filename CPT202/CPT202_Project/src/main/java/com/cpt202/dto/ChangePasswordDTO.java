package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for changing a user's own password.
 */
@Data
public class ChangePasswordDTO {

    /** The user's current password, used for identity verification. */
    @NotBlank(message = "Old password is required.")
    private String oldPassword;

    /** The new password to replace the current one. */
    @NotBlank(message = "New password is required.")
    @Size(min = 6, message = "New password must be at least 6 characters.")
    private String newPassword;
}
