package com.cpt202.vo;

import com.cpt202.model.entity.Project;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目展示对象。
 * <p>
 * 用于向不同端返回项目核心展示信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    /** 项目主键。 */
    private Long projectId;
    /** 教师主键。 */
    private Long teacherId;
    /** 教师姓名。 */
    private String teacherName;
    /** 分类主键。 */
    private Long categoryId;
    /** 分类名称。 */
    private String categoryName;
    /** 项目标题。 */
    private String title;
    /** 项目描述。 */
    private String description;
    /** 所需技能。 */
    private String requiredSkills;
    /** 主题方向。 */
    private String topicArea;
    /** 最大人数。 */
    private Integer maxStudents;
    /** 当前已同意人数。 */
    private Integer currentAgreedCount;
    /** 项目状态。 */
    private Project.ProjectStatus projectStatus;
    /** 发布时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime publishDate;
    /** 截止时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime closeDate;
}
