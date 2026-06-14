package com.cpt202.controller.admin;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.TwoFactorDisableDTO;
import com.cpt202.dto.TwoFactorEnableDTO;
import com.cpt202.result.Result;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.AdminProfileVO;
import com.cpt202.vo.TwoFactorSetupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin profile API.
 */
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
@Tag(name = "Admin Profile API")
public class AdminProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get current admin profile")
    public Result<AdminProfileVO> getMyProfile() {
        return Result.success(profileService.getAdminProfile(BaseContext.getCurrentUserId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current admin profile")
    public Result<Void> updateMyProfile(@Valid @RequestBody AdminProfileUpdateDTO adminProfileUpdateDTO) {
        profileService.updateAdminProfile(BaseContext.getCurrentUserId(), adminProfileUpdateDTO);
        return Result.success();
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current admin password")
    public Result<Void> changeMyPassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        profileService.changePassword(BaseContext.getCurrentUserId(), changePasswordDTO);
        return Result.success();
    }

    @PostMapping("/me/2fa/setup")
    @Operation(summary = "Initialize TOTP setup for current admin")
    public Result<TwoFactorSetupVO> initializeTwoFactorSetup() {
        return Result.success(profileService.initializeTwoFactorSetup(BaseContext.getCurrentUserId()));
    }

    @PostMapping("/me/2fa/enable")
    @Operation(summary = "Enable TOTP 2FA for current admin")
    public Result<Void> enableTwoFactor(@Valid @RequestBody TwoFactorEnableDTO twoFactorEnableDTO) {
        profileService.enableTwoFactor(BaseContext.getCurrentUserId(), twoFactorEnableDTO);
        return Result.success();
    }

    @PostMapping("/me/2fa/disable")
    @Operation(summary = "Disable TOTP 2FA for current admin")
    public Result<Void> disableTwoFactor(@Valid @RequestBody TwoFactorDisableDTO twoFactorDisableDTO) {
        profileService.disableTwoFactor(BaseContext.getCurrentUserId(), twoFactorDisableDTO);
        return Result.success();
    }
}
