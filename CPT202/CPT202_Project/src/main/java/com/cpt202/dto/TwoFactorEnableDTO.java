package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwoFactorEnableDTO {

    @NotBlank
    private String code;
}
