package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload returned when initializing TOTP setup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupVO {

    private Boolean enabled;
    private String manualEntryKey;
    private String otpAuthUri;
}
