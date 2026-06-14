package com.cpt202.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 教师资料展示对象。
 * <p>
 * 用于向前端返回教师基础资料和扩展资料。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfileVO {

    /** 教师主键。 */
    private Long teacherId;
    /** 用户名。 */
    private String username;
    /** 邮箱。 */
    private String email;
    /** 真实姓名。 */
    private String fullName;
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
    /** 是否已开启双重验证。 */
    private Boolean twoFactorEnabled;
    /** 更新时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
