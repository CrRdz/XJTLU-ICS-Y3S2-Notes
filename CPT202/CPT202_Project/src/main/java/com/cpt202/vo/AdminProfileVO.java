package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员资料展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileVO {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String accountStatus;
    private Boolean twoFactorEnabled;
}
