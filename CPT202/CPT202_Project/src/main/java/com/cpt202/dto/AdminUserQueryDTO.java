package com.cpt202.dto;

import com.cpt202.model.entity.User;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端用户查询参数。
 * <p>
 * 用于封装管理端查询用户列表时的角色和状态筛选条件。
 */
@Getter
@Setter
public class AdminUserQueryDTO extends PageQueryDTO {

    /** 用户角色筛选条件，可选。 */
    private User.UserRole role;
    /** 账号状态筛选条件，可选。 */
    private String accountStatus;
}
