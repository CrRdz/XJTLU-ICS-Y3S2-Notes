package com.cpt202.vo;

import com.cpt202.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录展示对象。
 * <p>
 * 用于向前端返回登录成功后的基础用户信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** 用户主键。 */
    private Long userId;
    /** 用户名。 */
    private String username;
    /** 真实姓名。 */
    private String fullName;
    /** 用户角色。 */
    private User.UserRole role;
    /** 账号状态。 */
    private String accountStatus;
    /** JWT 访问令牌。 */
    private String token;
    /** 是否需要继续完成双重验证。 */
    private Boolean twoFactorRequired;
    /** 双重验证登录挑战令牌。 */
    private String twoFactorChallengeToken;
}
