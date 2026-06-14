package com.cpt202.controller.teacher;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.dto.TwoFactorDisableDTO;
import com.cpt202.dto.TwoFactorEnableDTO;
import com.cpt202.result.Result;
import com.cpt202.service.ProfileService;
import com.cpt202.vo.TeacherProfileVO;
import com.cpt202.vo.TwoFactorSetupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端资料接口控制器。
 * 用于查询和维护教师个人资料。
 */
@RestController
@RequestMapping("/api/teacher/profile")
@Tag(name = "Teacher Profile API")
@Slf4j
public class TeacherProfileController {

    private final ProfileService profileService;

    public TeacherProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * 查询当前教师资料详情。
     *
     * @return 教师资料展示对象
     */
    @GetMapping("/me")
    @Operation(summary = "Get current teacher profile")
    public Result<TeacherProfileVO> getMyProfile() {
        Long teacherId = BaseContext.getCurrentUserId();
        log.info("Get teacher profile: {}", teacherId);
        return Result.success(profileService.getTeacherProfile(teacherId));
    }

    /**
     * 修改当前教师资料。
     *
     * @param teacherProfileUpdateDTO 教师资料更新参数
     * @return 统一成功响应
     */
    @PutMapping("/me")
    @Operation(summary = "Update current teacher profile")
    public Result<Void> updateMyProfile(@Valid @RequestBody TeacherProfileUpdateDTO teacherProfileUpdateDTO) {
        Long teacherId = BaseContext.getCurrentUserId();
        log.info("Update teacher profile: {}, payload: {}", teacherId, teacherProfileUpdateDTO);
        profileService.updateTeacherProfile(teacherId, teacherProfileUpdateDTO);
        return Result.success();
    }

    /**
     * 修改当前教师账号密码。
     * 需要提供旧密码进行身份验证。
     *
     * @param changePasswordDTO 修改密码参数（旧密码 + 新密码）
     * @return 统一成功响应
     */
    @PutMapping("/me/password")
    @Operation(summary = "Change current teacher password")
    public Result<Void> changeMyPassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Long teacherId = BaseContext.getCurrentUserId();
        log.info("Change password for teacher: {}", teacherId);
        profileService.changePassword(teacherId, changePasswordDTO);
        return Result.success();
    }

    @PostMapping("/me/2fa/setup")
    @Operation(summary = "Initialize TOTP setup for current teacher")
    public Result<TwoFactorSetupVO> initializeTwoFactorSetup() {
        return Result.success(profileService.initializeTwoFactorSetup(BaseContext.getCurrentUserId()));
    }

    @PostMapping("/me/2fa/enable")
    @Operation(summary = "Enable TOTP 2FA for current teacher")
    public Result<Void> enableTwoFactor(@Valid @RequestBody TwoFactorEnableDTO twoFactorEnableDTO) {
        profileService.enableTwoFactor(BaseContext.getCurrentUserId(), twoFactorEnableDTO);
        return Result.success();
    }

    @PostMapping("/me/2fa/disable")
    @Operation(summary = "Disable TOTP 2FA for current teacher")
    public Result<Void> disableTwoFactor(@Valid @RequestBody TwoFactorDisableDTO twoFactorDisableDTO) {
        profileService.disableTwoFactor(BaseContext.getCurrentUserId(), twoFactorDisableDTO);
        return Result.success();
    }
}
