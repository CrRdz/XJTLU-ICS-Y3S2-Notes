package com.cpt202.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.Project;
import com.cpt202.validation.ProjectValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 项目领域约束验证服务实现。
 */
@Service
public class ProjectValidationServiceImpl implements ProjectValidationService {

    @Override
    public void checkManualStatusChange(Project project, Project.ProjectStatus targetStatus) {
        if (targetStatus == Project.ProjectStatus.REQUESTED) {
            throw new BusinessException(MessageConstants.PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY);
        }
        if (targetStatus == Project.ProjectStatus.ARCHIVED) {
            throw new BusinessException(MessageConstants.PROJECT_STATUS_ARCHIVED_DISABLED);
        }
        if (project.getProjectStatus() == Project.ProjectStatus.CLOSED
                && targetStatus != Project.ProjectStatus.CLOSED) {
            throw new BusinessException(MessageConstants.PROJECT_STATUS_TRANSITION_INVALID);
        }
    }

    @Override
    public void checkProjectCloseDate(LocalDateTime closeDate) {
        if (closeDate == null || !closeDate.isAfter(LocalDateTime.now())) {
            throw new BusinessException(MessageConstants.PROJECT_CLOSE_DATE_INVALID);
        }
    }

    @Override
    public void checkProjectOwnership(Project project, Long teacherId) {
        if (project.getTeacher() == null || !teacherId.equals(project.getTeacher().getTeacherId())) {
            throw new BusinessException(MessageConstants.CANNOT_OPERATE_OTHER_TEACHER_PROJECT);
        }
    }
}
