package com.cpt202.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目新增/修改参数。
 * <p>
 * 用于教师创建项目或修改项目信息时接收请求体。
 */
@Data
public class ProjectDTO {

    /** 项目所属分类主键。 */
    @NotNull
    private Long categoryId;

    /** 项目标题。 */
    @NotBlank
    private String title;

    /** 项目详细描述。 */
    @NotBlank
    private String description;

    /** 所需技能说明。 */
    private String requiredSkills;
    /** 研究主题或方向。 */
    private String topicArea;

    /** 项目允许的最大参与人数。 */
    @Min(1)
    private Integer maxStudents;

    /** 学生申请该项目的截止时间。 */
    @NotNull
    private LocalDateTime closeDate;
}
