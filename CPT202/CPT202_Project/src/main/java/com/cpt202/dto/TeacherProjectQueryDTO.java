package com.cpt202.dto;

import com.cpt202.model.entity.Project;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师端项目查询参数。
 * <p>
 * 用于封装教师查看本人项目列表时的筛选条件。
 */
@Getter
@Setter
public class TeacherProjectQueryDTO extends PageQueryDTO {
    /** 项目状态筛选条件，可选。 */
    private Project.ProjectStatus status;
}
