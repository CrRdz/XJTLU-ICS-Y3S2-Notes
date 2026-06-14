package com.cpt202.validation;

import com.cpt202.model.entity.Project;

import java.time.LocalDateTime;

/**
 * 项目领域约束验证服务。
 * 负责项目状态流转规则、教师归属、志愿顺位唯一性等项目相关约束。
 */
public interface ProjectValidationService {

    /**
     * 校验教师手动变更项目状态是否合法。
     *
     * @param project      当前项目
     * @param targetStatus 目标状态
     */
    void checkManualStatusChange(Project project, Project.ProjectStatus targetStatus);

    /**
     * 校验项目申请截止时间是否合法。
     *
     * @param closeDate 项目申请截止时间
     */
    void checkProjectCloseDate(LocalDateTime closeDate);

    /**
     * 校验教师是否为指定项目的所有者。
     *
     * @param project   项目实体
     * @param teacherId 教师主键
     */
    void checkProjectOwnership(Project project, Long teacherId);
}
