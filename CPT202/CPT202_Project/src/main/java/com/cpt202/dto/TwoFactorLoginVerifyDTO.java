package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorLoginVerifyDTO {

    @NotBlank
    private String challengeToken;

    @NotBlank
    private String code;
}
