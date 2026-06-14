package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectRequest.RequestStatus;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.ProjectRequestValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRequestValidationServiceImpl implements ProjectRequestValidationService {

    private static final List<RequestStatus> ACTIVE_REQUEST_STATUSES = List.of(
            RequestStatus.PENDING,
            RequestStatus.ACCEPTED
    );
    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    @Override
    public void validateRequest(Long studentId, Project project) {
        // 1. 截止日期校验
        if (project != null
                && project.getCloseDate() != null
                && LocalDateTime.now().isAfter(project.getCloseDate())) {
            throw new RuleViolationException(MessageConstants.REQUEST_DEADLINE_PASSED);
        }

        // 2. 已关闭或已归档的项目不能接收新申请。
        if (project == null
                || project.getProjectStatus() == Project.ProjectStatus.CLOSED
                || project.getProjectStatus() == Project.ProjectStatus.ARCHIVED) {
            throw new RuleViolationException(MessageConstants.PROJECT_NOT_ACCEPTING_REQUESTS);
        }

        if (requestRepository.existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(
                studentId,
                project.getProjectId(),
                ACTIVE_REQUEST_STATUSES
        )) {
            throw new RuleViolationException(MessageConstants.PROJECT_REQUEST_ALREADY_EXISTS);
        }

        // 3. 只限制已经持有 agreed 项目的学生继续申请。
        boolean alreadyHasRequest = requestRepository.existsByStudent_StudentIdAndRequestStatusIn(
                studentId,
                List.of(RequestStatus.ACCEPTED)
        );

        if (alreadyHasRequest) {
            throw new RuleViolationException(MessageConstants.ACTIVE_REQUEST_EXISTS);
        }
    }

    @Transactional
    @Override
    public void onApprovalSuccess(Long requestId) {
        ProjectRequest currentRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuleViolationException(MessageConstants.REQUEST_RECORD_NOT_FOUND));

        Long studentId = currentRequest.getStudent().getStudentId();
        Long projectId = currentRequest.getProject().getProjectId();

        List<ProjectRequest> others = requestRepository.findByStudent_StudentIdAndRequestStatus(studentId, RequestStatus.PENDING);
        for (ProjectRequest request : others) {
            if (request.getRequestId().equals(requestId)) {
                continue;
            }

            RequestStatus oldStatus = request.getRequestStatus();
            LocalDateTime now = LocalDateTime.now();
            request.setRequestStatus(RequestStatus.REJECTED);
            request.setDecisionComment(MessageConstants.AUTO_REJECT_ALREADY_MATCHED);
            request.setUpdatedAt(now);

            RequestStatusHistory history = new RequestStatusHistory();
            history.setRequest(request);
            history.setOldStatus(oldStatus == null ? null : oldStatus.name());
            history.setNewStatus(RequestStatus.REJECTED.name());
            history.setChangedBy(null);
            history.setRemark(MessageConstants.AUTO_REJECT_ALREADY_MATCHED_REMARK);
            history.setChangedAt(now);
            requestStatusHistoryRepository.save(history);
        }
        requestRepository.saveAll(others);

        Project project = projectRepository.findById(projectId).orElseThrow();
        long currentCount = requestRepository.countByProject_ProjectIdAndRequestStatus(projectId, RequestStatus.ACCEPTED);
        project.setCurrentAgreedCount((int) currentCount);
        project.setUpdatedAt(LocalDateTime.now());

        if (currentCount >= project.getMaxStudents()) {
            project.setProjectStatus(Project.ProjectStatus.CLOSED);
            project.setCloseDate(LocalDateTime.now());
        } else {
            project.setProjectStatus(Project.ProjectStatus.AGREED);
        }
        projectRepository.save(project);
    }
}
