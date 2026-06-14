package com.cpt202.controller.admin;

import com.cpt202.dto.AdminUserQueryDTO;
import com.cpt202.dto.AdminUserUpdateDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Admin user interface controller.
 * Provides capabilities for querying user lists and modifying account status.
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User API")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * Filter user list by role and account status.
     *
     * @param queryDTO User query parameters
     * @return A paged list of user view objects (VOs)
     */
    @GetMapping
    @Operation(summary = "List users")
    public Result<PageResult<UserVO>> list(@Valid AdminUserQueryDTO queryDTO) {
        return Result.success(userAdminService.listUsersPage(queryDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "List users by page")
    public Result<PageResult<UserVO>> listPage(@Valid AdminUserQueryDTO queryDTO) {
        return Result.success(userAdminService.listUsersPage(queryDTO));
    }

    /**
     * Update the account status of a specific user.
     *
     * @param userId User primary key
     * @param accountStatus Account status
     * @return Unified success response
     */
    @PutMapping("/{userId}/status")
    @Operation(summary = "Update user status")
    public Result<Void> updateStatus(@PathVariable Long userId,
                                     @RequestParam String accountStatus) {
        userAdminService.updateStatus(userId, accountStatus);
        return Result.success();
    }

    /**
     * Update the basic information of a specific user.
     *
     * @param userId User primary key
     * @param updateDTO User basic information parameters
     * @return Unified success response
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user basic information")
    public Result<Void> updateUser(@PathVariable Long userId,
                                   @Valid @RequestBody AdminUserUpdateDTO updateDTO) {
        userAdminService.updateUser(userId, updateDTO);
        return Result.success();
    }
}