package com.cpt202.vo;

import com.cpt202.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户展示对象。
 * <p>
 * 主要用于管理端返回用户列表与用户基本信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    /** 用户主键。 */
    private Long userId;
    /** 用户名。 */
    private String username;
    /** 邮箱。 */
    private String email;
    /** 真实姓名。 */
    private String fullName;
    /** 用户角色。 */
    private User.UserRole role;
    /** 账号状态。 */
    private String accountStatus;
}
