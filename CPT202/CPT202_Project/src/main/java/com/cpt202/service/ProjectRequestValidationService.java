package com.cpt202.service;

import com.cpt202.model.entity.Project;

public interface ProjectRequestValidationService {

    void validateRequest(Long studentId, Project project);

    void onApprovalSuccess(Long requestId);
}
