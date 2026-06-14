package com.cpt202.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 项目申请创建参数。
 * <p>
 * 用于学生提交项目申请时接收请求体。
 */
@Data
public class ProjectRequestCreateDTO {

    /** 目标项目主键。 */
    @jakarta.validation.constraints.NotNull
    private Long projectId;

    /** 学生对该申请的志愿顺位。 */
    @Min(1)
    private Integer preferenceRank;

    /** 学生提交的补充说明。 */
    private String notes;
}
