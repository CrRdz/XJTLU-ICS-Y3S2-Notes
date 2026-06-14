package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员资料更新参数。
 */
@Data
public class AdminProfileUpdateDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    private String email;
}
