package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.dto.StudentProjectRequestQueryDTO;
import com.cpt202.dto.TeacherProjectRequestQueryDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.result.PageResult;
import com.cpt202.service.ProjectRequestValidationService;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.util.VoConverter;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.StudentRequestSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Project Request Service Implementation.
 * Integrates Module 8 business rules for project selection.
 */
@Service
@RequiredArgsConstructor
public class ProjectRequestServiceImpl implements ProjectRequestService {

    private static final List<ProjectRequest.RequestStatus> ACTIVE_REQUEST_STATUSES = List.of(
            ProjectRequest.RequestStatus.PENDING,
            ProjectRequest.RequestStatus.ACCEPTED
    );
    private final ProjectRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final StudentProfileRepository studentRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;
    private final ProjectRequestValidationService projectRequestValidationService;

    /**
     * Student submits a project request.
     */
    @Override
    @Transactional
    public void create(Long studentId, ProjectRequestCreateDTO dto) {
        // 1. 查找关联实体
        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuleViolationException(MessageConstants.STUDENT_RECORD_NOT_FOUND));
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuleViolationException(MessageConstants.PROJECT_RECORD_NOT_FOUND));

        // 2. 检查截止日期、项目状态和分配限制
        projectRequestValidationService.validateRequest(studentId, project);
        validatePreferenceRank(studentId, dto.getPreferenceRank());

        // 3. 构建并保存申请记录
        LocalDateTime now = LocalDateTime.now();
        ProjectRequest request = new ProjectRequest();
        BeanUtils.copyProperties(dto, request, "projectId");
        request.setRequestStatus(ProjectRequest.RequestStatus.PENDING);
        request.setSubmittedAt(now);
        request.setUpdatedAt(now);
        request.setStudent(student);
        request.setProject(project);

        request = requestRepository.save(request);
        syncProjectStatusAfterRequestChange(project.getProjectId());
        saveHistory(request, null, ProjectRequest.RequestStatus.PENDING, student, MessageConstants.REQUEST_SUBMIT_REMARK);
    }

    /**
     * 教师审核项目申请。
     */
    @Override
    @Transactional
    public void review(Long requestId, Long teacherId, ProjectRequestReviewDTO dto) {

        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));

        if (request.getProject() == null
                || request.getProject().getTeacher() == null
                || !teacherId.equals(request.getProject().getTeacher().getTeacherId())) {
            throw new BusinessException(MessageConstants.CANNOT_REVIEW_OTHER_TEACHER_REQUEST);
        }

        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TEACHER_NOT_FOUND));

        ProjectRequest.RequestStatus oldStatus = request.getRequestStatus();
        if (oldStatus != ProjectRequest.RequestStatus.PENDING) {
            throw new RuleViolationException(MessageConstants.REQUEST_NOT_PENDING_CANNOT_REVIEW);
        }
        if (dto.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED) {
            long currentAccepted = requestRepository.countByProject_ProjectIdAndRequestStatus(
                    request.getProject().getProjectId(),
                    ProjectRequest.RequestStatus.ACCEPTED
            );
            if (currentAccepted >= request.getProject().getMaxStudents()) {
                throw new RuleViolationException(MessageConstants.PROJECT_CAPACITY_EXCEEDED);
            }
        }
        request.setRequestStatus(dto.getRequestStatus());
        request.setDecisionComment(dto.getDecisionComment());
        request.setReviewedBy(teacher);
        request.setReviewedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        requestRepository.save(request);
        saveHistory(request, oldStatus, dto.getRequestStatus(), null, dto.getDecisionComment());

        if (dto.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED) {
            projectRequestValidationService.onApprovalSuccess(requestId);
        } else {
            syncProjectStatusAfterRequestChange(request.getProject().getProjectId());
        }
    }

    @Override
    public List<ProjectRequestVO> listStudentRequests(Long studentId) {
        return VoConverter.toList(
                requestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentId),
                this::toProjectRequestVO);
    }

        @Override
        public StudentRequestSummaryVO getStudentRequestSummary(Long studentId) {
        Page<ProjectRequestVO> recentRequestPage = requestRepository.findStudentRequestVos(studentId, PageRequest.of(0, 3));
        List<ProjectRequest> withdrawnRequests = requestRepository.findByStudent_StudentIdAndRequestStatus(
            studentId,
            ProjectRequest.RequestStatus.WITHDRAWN
        );
        List<Object[]> statusCounts = requestRepository.countStudentRequestsByStatus(studentId);

        return StudentRequestSummaryVO.builder()
            .totalRequests(statusCounts.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum())
            .pendingCount(findCount(statusCounts, ProjectRequest.RequestStatus.PENDING))
            .acceptedCount(findCount(statusCounts, ProjectRequest.RequestStatus.ACCEPTED))
            .rejectedCount(findCount(statusCounts, ProjectRequest.RequestStatus.REJECTED))
            .withdrawnCount(findCount(statusCounts, ProjectRequest.RequestStatus.WITHDRAWN))
            .withdrawnProjectIds(withdrawnRequests.stream()
                .map(request -> request.getProject() == null ? null : request.getProject().getProjectId())
                .filter(projectId -> projectId != null)
                .distinct()
                .toList())
            .recentRequests(recentRequestPage.getContent())
            .build();
        }

        @Override
        public List<ProjectRequestVO> getStudentRequestContext(Long studentId, Long projectId) {
        Map<Long, ProjectRequestVO> requestContext = new LinkedHashMap<>();

        requestRepository.findByStudent_StudentIdAndProject_ProjectIdOrderBySubmittedAtDesc(studentId, projectId)
            .stream()
            .map(this::toProjectRequestVO)
            .forEach(request -> requestContext.put(request.getRequestId(), request));

        requestRepository.findByStudent_StudentIdAndRequestStatusInOrderBySubmittedAtDesc(
                studentId,
                ACTIVE_REQUEST_STATUSES)
            .stream()
            .map(this::toProjectRequestVO)
            .forEach(request -> requestContext.putIfAbsent(request.getRequestId(), request));

        return requestContext.values().stream()
            .sorted(Comparator.comparing(ProjectRequestVO::getSubmittedAt,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ProjectRequestVO::getRequestId, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
        }

    @Override
    public PageResult<ProjectRequestVO> listStudentRequestsPage(Long studentId, StudentProjectRequestQueryDTO queryDTO) {
        Page<ProjectRequestVO> requestPage = requestRepository.findStudentRequestVos(studentId, toPageable(queryDTO));
        return PageResult.fromPage(requestPage);
    }

    @Override
    public List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status) {
        List<ProjectRequest> requests = status == null
                ? requestRepository.findByProject_Teacher_TeacherIdOrderBySubmittedAtDesc(teacherId)
                : requestRepository.findByProject_Teacher_TeacherIdAndRequestStatusOrderBySubmittedAtDesc(teacherId, status);
        return VoConverter.toList(requests, this::toProjectRequestVO);
    }

    @Override
    public ProjectRequestVO getTeacherRequest(Long requestId, Long teacherId) {
        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));
        if (request.getProject() == null
                || request.getProject().getTeacher() == null
                || !teacherId.equals(request.getProject().getTeacher().getTeacherId())) {
            throw new BusinessException(MessageConstants.CANNOT_REVIEW_OTHER_TEACHER_REQUEST);
        }
        return toProjectRequestVO(request);
    }

    @Override
    public PageResult<ProjectRequestVO> listTeacherRequestsPage(Long teacherId, TeacherProjectRequestQueryDTO queryDTO) {
        boolean historyOnly = Boolean.TRUE.equals(queryDTO.getHistoryOnly());
        Page<ProjectRequestVO> requestPage = historyOnly
            ? requestRepository.findTeacherHistoryVos(
                teacherId,
                queryDTO.getStatus(),
                ProjectRequest.RequestStatus.PENDING,
                toPageable(queryDTO))
                : requestRepository.findTeacherRequestVos(teacherId, queryDTO.getStatus(), toPageable(queryDTO));
        return PageResult.fromPage(requestPage);
    }

    @Override
    public void withdraw(Long requestId, Long studentId) {
        ProjectRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));
        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException(MessageConstants.CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST);
        }

        ProjectRequest.RequestStatus oldStatus = request.getRequestStatus();
        request.setRequestStatus(ProjectRequest.RequestStatus.WITHDRAWN);
        request.setWithdrawnAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        requestRepository.save(request);
        syncProjectStatusAfterRequestChange(request.getProject().getProjectId());
        saveHistory(request, oldStatus, ProjectRequest.RequestStatus.WITHDRAWN, request.getStudent(), MessageConstants.REQUEST_WITHDRAW_REMARK);
    }

    private ProjectRequestVO toProjectRequestVO(ProjectRequest request) {
        ProjectRequestVO requestVO = new ProjectRequestVO();
        BeanUtils.copyProperties(request, requestVO);
        requestVO.setProjectId(request.getProject() == null ? null : request.getProject().getProjectId());
        requestVO.setProjectTitle(request.getProject() == null ? null : request.getProject().getTitle());
        requestVO.setStudentId(request.getStudent() == null ? null : request.getStudent().getStudentId());
        requestVO.setStudentName(request.getStudent() == null || request.getStudent().getUser() == null
            ? null : request.getStudent().getUser().getFullName());
        requestVO.setStudentNo(request.getStudent() == null ? null : request.getStudent().getStudentNo());
        requestVO.setStudentEmail(request.getStudent() == null || request.getStudent().getUser() == null
            ? null : request.getStudent().getUser().getEmail());
        requestVO.setStudentProgramme(request.getStudent() == null ? null : request.getStudent().getProgramme());
        requestVO.setStudentPhone(request.getStudent() == null ? null : request.getStudent().getPhone());
        requestVO.setStudentInterests(request.getStudent() == null ? null : request.getStudent().getInterests());
        requestVO.setReviewedByTeacherId(request.getReviewedBy() == null ? null : request.getReviewedBy().getTeacherId());
        return requestVO;
    }

        private long findCount(List<Object[]> counts, ProjectRequest.RequestStatus status) {
        return counts.stream()
            .filter(row -> row[0] == status)
            .map(row -> (Number) row[1])
            .findFirst()
            .map(Number::longValue)
            .orElse(0L);
        }

    private void saveHistory(ProjectRequest request,
                             ProjectRequest.RequestStatus oldStatus,
                             ProjectRequest.RequestStatus newStatus,
                             StudentProfile changedBy,
                             String remark) {
        RequestStatusHistory history = new RequestStatusHistory();
        history.setRequest(request);
        history.setOldStatus(oldStatus == null ? null : oldStatus.name());
        history.setNewStatus(newStatus.name());
        history.setChangedBy(changedBy);
        history.setRemark(remark);
        history.setChangedAt(LocalDateTime.now());
        requestStatusHistoryRepository.save(history);
    }

    private void validatePreferenceRank(Long studentId, Integer preferenceRank) {
        if (preferenceRank == null) {
            return;
        }
        if (requestRepository.existsByStudent_StudentIdAndPreferenceRankAndRequestStatusIn(
                studentId,
                preferenceRank,
                ACTIVE_REQUEST_STATUSES
        )) {
            throw new RuleViolationException(MessageConstants.PROJECT_PREFERENCE_RANK_DUPLICATED);
        }
    }

    private void syncProjectStatusAfterRequestChange(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.PROJECT_RECORD_NOT_FOUND));

        long acceptedCount = requestRepository.countByProject_ProjectIdAndRequestStatus(
                projectId,
                ProjectRequest.RequestStatus.ACCEPTED
        );
        long pendingCount = requestRepository.countByProject_ProjectIdAndRequestStatus(
                projectId,
                ProjectRequest.RequestStatus.PENDING
        );

        project.setCurrentAgreedCount((int) acceptedCount);
        if (acceptedCount >= project.getMaxStudents()) {
            project.setProjectStatus(Project.ProjectStatus.CLOSED);
            project.setCloseDate(LocalDateTime.now());
        } else if (acceptedCount > 0) {
            project.setProjectStatus(Project.ProjectStatus.AGREED);
            project.setCloseDate(null);
        } else if (pendingCount > 0) {
            project.setProjectStatus(Project.ProjectStatus.REQUESTED);
            project.setCloseDate(null);
        } else if (project.getProjectStatus() != Project.ProjectStatus.CLOSED) {
            project.setProjectStatus(Project.ProjectStatus.AVAILABLE);
            project.setCloseDate(null);
        }
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    private Pageable toPageable(PageQueryDTO queryDTO) {
        return PageRequest.of(
                Math.max(0, queryDTO.getPageNum() - 1),
                queryDTO.getPageSize());
    }
}
