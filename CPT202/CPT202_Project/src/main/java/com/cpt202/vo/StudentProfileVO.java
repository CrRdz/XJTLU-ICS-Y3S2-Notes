package com.cpt202.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生资料展示对象。
 * <p>
 * 用于向前端返回学生基础资料和扩展资料。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileVO {

    /** 学生主键。 */
    private Long studentId;
    /** 用户名。 */
    private String username;
    /** 邮箱。 */
    private String email;
    /** 真实姓名。 */
    private String fullName;
    /** 学号。 */
    private String studentNo;
    /** 专业。 */
    private String programme;
    /** 入学日期。 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate enrollmentDate;
    /** 学年（由入学日期动态计算）。 */
    private Integer academicYear;
    /** 联系电话。 */
    private String phone;
    /** 兴趣方向。 */
    private String interests;
    /** 是否已开启双重验证。 */
    private Boolean twoFactorEnabled;
    /** 更新时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
