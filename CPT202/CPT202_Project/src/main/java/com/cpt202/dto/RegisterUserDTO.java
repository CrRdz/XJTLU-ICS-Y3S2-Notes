package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户注册参数。
 * <p>
 * 同时承载通用账号字段与学生/教师角色专属资料字段，
 * 具体启用哪些字段由注册角色决定。
 */
@Data
public class RegisterUserDTO {

    /** 登录用户名。 */
    @NotBlank
    private String username;

    /** 登录密码。 */
    @NotBlank
    private String password;

    /** 用户邮箱。 */
    @Email
    @NotBlank
    private String email;

    /** 邮箱验证码。 */
    @NotBlank
    private String otp;

    /** 用户真实姓名。 */
    @NotBlank
    private String fullName;

    /** 学号。 */
    private String studentNo;
    /** 专业。 */
    private String programme;
    /** 入学日期。 */
    @PastOrPresent(message = "Enrollment date cannot be in the future.")
    private LocalDate enrollmentDate;
    /** 联系电话。 */
    private String phone;
    /** 兴趣方向。 */
    private String interests;

    /** 工号。 */
    private String staffNo;
    /** 院系。 */
    private String department;
    /** 职称。 */
    private String title;
    /** 研究方向。 */
    private String researchArea;
    /** 办公地点。 */
    private String office;
}
