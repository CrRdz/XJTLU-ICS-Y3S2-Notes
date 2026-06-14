package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorDisableDTO {

    @NotBlank
    private String currentPassword;
}
