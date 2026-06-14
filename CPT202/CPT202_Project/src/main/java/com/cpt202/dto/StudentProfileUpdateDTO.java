package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 学生资料更新参数。
 * 同时包含用户主表中的基础信息与学生资料表中的扩展信息。
 */
@Data
public class StudentProfileUpdateDTO {

    /** 学生姓名。 */
    @NotBlank
    private String fullName;

    /** 学生邮箱。 */
    @NotBlank
    @Email(message = "Invalid email format.")
    private String email;

    /** 专业。 */
    private String programme;
    /** 入学日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @PastOrPresent(message = "Enrollment date cannot be in the future.")
    private LocalDate enrollmentDate;
    /** 联系电话。 */
    private String phone;
    /** 兴趣方向。 */
    private String interests;
}
