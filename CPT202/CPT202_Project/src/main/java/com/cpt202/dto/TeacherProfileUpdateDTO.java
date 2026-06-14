package com.cpt202.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 教师资料更新参数。
 * <p>
 * 同时包含用户主表中的基础信息与教师资料表中的扩展信息。
 */
@Data
public class TeacherProfileUpdateDTO {

    /** 教师姓名。 */
    @NotBlank
    private String fullName;

    /** 教师邮箱。 */
    @NotBlank
    @Email(message = "Invalid email format.")
    private String email;

    /** 所属院系。 */
    private String department;
    /** 职称。 */
    private String title;
    /** 研究方向。 */
    private String researchArea;
    /** 办公地点。 */
    private String office;
}
