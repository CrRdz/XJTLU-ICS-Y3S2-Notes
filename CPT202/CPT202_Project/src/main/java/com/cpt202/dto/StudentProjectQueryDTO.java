package com.cpt202.dto;

import com.cpt202.model.entity.Project;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

/**
 * 学生端项目查询参数。
 * <p>
 * 用于封装学生查看项目列表时的筛选条件。
 */
@Data
public class StudentProjectQueryDTO {

    /** 项目标题、主题等关键字模糊搜索条件。 */
    private String keyword;
    /** 分类筛选条件。 */
    private Long categoryId;
    /** 项目状态筛选条件。 */
    private Project.ProjectStatus status;

    private List<Long> tagIds;

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;
    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 50, message = "pageSize must be <= 50")
    private Integer pageSize = 10;
}
