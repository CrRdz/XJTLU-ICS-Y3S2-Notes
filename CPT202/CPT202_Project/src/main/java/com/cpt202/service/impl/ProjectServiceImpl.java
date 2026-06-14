package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.StudentProjectQueryDTO;
import com.cpt202.dto.TeacherProjectQueryDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.ProjectStatusHistory;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.ProjectStatusHistoryRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.result.PageResult;
import com.cpt202.service.ProjectService;
import com.cpt202.util.ProjectSearchRelevanceScorer;
import com.cpt202.util.VoConverter;
import com.cpt202.validation.ProjectValidationService;
import com.cpt202.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 项目服务实现类。
 * 负责项目查询、创建、修改和状态流转等业务逻辑的实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectStatusHistoryRepository projectStatusHistoryRepository;
    private final ProjectRequestRepository projectRequestRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;
    private final ProjectValidationService projectValidationService;

    /**
     * 查询学生端项目列表（DB 端过滤 + 分页）。
     */
    @Override
    public PageResult<ProjectVO> listStudentProjects(StudentProjectQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (hasKeyword(queryDTO.getKeyword())) {
            return listStudentProjectsByRelevance(queryDTO, pageNum, pageSize, defaultSort);
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, pageNum - 1),
                pageSize,
                defaultSort);

        Page<Project> projectPage = projectRepository.findStudentProjects(
                queryDTO.getKeyword(),
                queryDTO.getCategoryId(),
                queryDTO.getStatus(),
                queryDTO.getTagIds(),
                pageable);
        List<ProjectVO> projectVos = VoConverter.toList(projectPage.getContent(), this::toProjectVO);
        return new PageResult<>(
                projectPage.getTotalElements(),
                projectVos,
                projectPage.getNumber() + 1,
                projectPage.getSize(),
                projectPage.getTotalPages());
    }

    private PageResult<ProjectVO> listStudentProjectsByRelevance(StudentProjectQueryDTO queryDTO,
                                                                 int pageNum,
                                                                 int pageSize,
                                                                 Sort defaultSort) {
        List<ProjectSearchResult> rankedProjects = projectRepository.findStudentProjectCandidates(
                        queryDTO.getCategoryId(),
                        queryDTO.getStatus(),
                        queryDTO.getTagIds(),
                        defaultSort).stream()
                .map(project -> new ProjectSearchResult(project,
                        ProjectSearchRelevanceScorer.score(project, queryDTO.getKeyword())))
                .filter(result -> result.score() > 0.0)
                .sorted(Comparator
                        .comparingDouble(ProjectSearchResult::score).reversed()
                        .thenComparing(result -> result.project().getCreatedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(result -> result.project().getProjectId(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int fromIndex = Math.min(Math.max(0, pageNum - 1) * pageSize, rankedProjects.size());
        int toIndex = Math.min(fromIndex + pageSize, rankedProjects.size());
        List<ProjectVO> records = rankedProjects.subList(fromIndex, toIndex).stream()
                .map(ProjectSearchResult::project)
                .map(this::toProjectVO)
                .toList();
        int totalPages = rankedProjects.isEmpty() ? 0 : (int) Math.ceil((double) rankedProjects.size() / pageSize);

        return new PageResult<>(
                (long) rankedProjects.size(),
                records,
                pageNum,
                pageSize,
                totalPages);
    }

    private boolean hasKeyword(String keyword) {
        return keyword != null && !keyword.trim().isEmpty();
    }

    private record ProjectSearchResult(Project project, double score) {
    }

    /**
     * 查询教师端项目列表。
     */
    @Override
    public List<ProjectVO> listTeacherProjects(Long teacherId, Project.ProjectStatus status) {
        return filterByDerivedStatus(
                VoConverter.toList(projectRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherId), this::toProjectVO),
                status);
    }

    @Override
    public PageResult<ProjectVO> listTeacherProjectsPage(Long teacherId, TeacherProjectQueryDTO queryDTO) {
        int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();
        List<ProjectVO> projects = listTeacherProjects(teacherId, queryDTO.getStatus());
        int fromIndex = Math.min(Math.max(0, pageNum - 1) * pageSize, projects.size());
        int toIndex = Math.min(fromIndex + pageSize, projects.size());
        int totalPages = projects.isEmpty() ? 0 : (int) Math.ceil((double) projects.size() / pageSize);
        return new PageResult<>(
                (long) projects.size(),
                projects.subList(fromIndex, toIndex),
                pageNum,
                pageSize,
                totalPages);
    }

    /**
     * 查询项目详情。
     */
    @Override
    public ProjectVO getProject(Long projectId) {
        return toProjectVO(getProjectEntity(projectId));
    }

    @Override
    public ProjectVO getOwnedProject(Long projectId, Long teacherId) {
        return toProjectVO(getOwnedProjectEntity(projectId, teacherId));
    }

    /**
     * 新增项目。
     */
    @Override
    @Transactional
    public ProjectVO create(Long teacherId, ProjectDTO projectDTO) {
        TeacherProfile teacher = teacherProfileRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.TEACHER_NOT_FOUND));
        projectValidationService.checkProjectCloseDate(projectDTO.getCloseDate());

        LocalDateTime now = LocalDateTime.now();
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project, "categoryId");
        project.setTeacher(teacher);
        project.setCategory(categoryRepository.findById(projectDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException(MessageConstants.PROJECT_CATEGORY_NOT_FOUND)));
        project.setCurrentAgreedCount(0);
        project.setProjectStatus(Project.ProjectStatus.AVAILABLE);
        project.setPublishDate(now);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        Project savedProject = projectRepository.save(project);
        return toProjectVO(savedProject);
    }

    /**
     * 修改项目。
     */
    @Override
    @Transactional
    public void update(Long projectId, Long teacherId, ProjectDTO projectDTO) {
        Project project = getOwnedProjectEntity(projectId, teacherId);
        if (project.getProjectStatus() != Project.ProjectStatus.CLOSED
                && project.getProjectStatus() != Project.ProjectStatus.ARCHIVED) {
            projectValidationService.checkProjectCloseDate(projectDTO.getCloseDate());
        }
        BeanUtils.copyProperties(projectDTO, project, "categoryId");
        project.setCategory(categoryRepository.findById(projectDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException(MessageConstants.PROJECT_CATEGORY_NOT_FOUND)));
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    /**
     * 修改项目状态。
     */
    @Override
    @Transactional
    public void changeStatus(Long projectId, Long teacherId, ProjectStatusUpdateDTO projectStatusUpdateDTO) {
        Project project = getOwnedProjectEntity(projectId, teacherId);
        projectValidationService.checkManualStatusChange(project, projectStatusUpdateDTO.getProjectStatus());
        Project.ProjectStatus oldStatus = project.getProjectStatus();
        LocalDateTime now = LocalDateTime.now();
        project.setProjectStatus(projectStatusUpdateDTO.getProjectStatus());
        project.setUpdatedAt(now);
        if (projectStatusUpdateDTO.getProjectStatus() == Project.ProjectStatus.CLOSED
                || projectStatusUpdateDTO.getProjectStatus() == Project.ProjectStatus.ARCHIVED) {
            project.setCloseDate(now);
            cancelActiveRequestsForClosedProject(project, now);
        }
        projectRepository.save(project);

        ProjectStatusHistory history = new ProjectStatusHistory();
        history.setProject(project);
        history.setOldStatus(oldStatus == null ? null : oldStatus.name());
        history.setNewStatus(projectStatusUpdateDTO.getProjectStatus().name());
        history.setChangedBy(project.getTeacher());
        history.setRemark(projectStatusUpdateDTO.getRemark());
        history.setChangedAt(now);
        projectStatusHistoryRepository.save(history);
    }

    // --- Validation logic moved to ProjectValidationService ---

    private void cancelActiveRequestsForClosedProject(Project project, LocalDateTime changedAt) {
        List<ProjectRequest> activeRequests = projectRequestRepository.findByProject_ProjectIdAndRequestStatusIn(
                project.getProjectId(),
                List.of(ProjectRequest.RequestStatus.PENDING, ProjectRequest.RequestStatus.ACCEPTED)
        );

        for (ProjectRequest request : activeRequests) {
            ProjectRequest.RequestStatus oldStatus = request.getRequestStatus();
            request.setRequestStatus(ProjectRequest.RequestStatus.REJECTED);
            request.setDecisionComment(MessageConstants.PROJECT_CLOSED_AND_REQUEST_CANCELLED);
            request.setReviewedAt(changedAt);
            request.setUpdatedAt(changedAt);

            RequestStatusHistory history = new RequestStatusHistory();
            history.setRequest(request);
            history.setOldStatus(oldStatus == null ? null : oldStatus.name());
            history.setNewStatus(ProjectRequest.RequestStatus.REJECTED.name());
            history.setChangedBy(null);
            history.setRemark(MessageConstants.AUTO_CANCEL_PROJECT_CLOSED_REMARK);
            history.setChangedAt(changedAt);
            requestStatusHistoryRepository.save(history);
        }

        if (!activeRequests.isEmpty()) {
            projectRequestRepository.saveAll(activeRequests);
        }
        project.setCurrentAgreedCount(0);
    }
    private Project getProjectEntity(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.PROJECT_NOT_FOUND));
    }

    private Project getOwnedProjectEntity(Long projectId, Long teacherId) {
        Project project = getProjectEntity(projectId);
        projectValidationService.checkProjectOwnership(project, teacherId);
        return project;
    }

    private ProjectVO toProjectVO(Project project) {
        ProjectVO projectVO = new ProjectVO();
        BeanUtils.copyProperties(project, projectVO);
        long acceptedCount = projectRequestRepository.countByProject_ProjectIdAndRequestStatus(
                project.getProjectId(),
                ProjectRequest.RequestStatus.ACCEPTED
        );
        long pendingCount = projectRequestRepository.countByProject_ProjectIdAndRequestStatus(
                project.getProjectId(),
                ProjectRequest.RequestStatus.PENDING
        );

        projectVO.setCurrentAgreedCount((int) acceptedCount);
        projectVO.setProjectStatus(resolveDisplayStatus(project, acceptedCount, pendingCount));
        projectVO.setTeacherId(project.getTeacher() == null ? null : project.getTeacher().getTeacherId());
        projectVO.setTeacherName(project.getTeacher() == null || project.getTeacher().getUser() == null
                ? null : project.getTeacher().getUser().getFullName());
        projectVO.setCategoryId(project.getCategory() == null ? null : project.getCategory().getCategoryId());
        projectVO.setCategoryName(project.getCategory() == null ? null : project.getCategory().getCategoryName());
        return projectVO;
    }

    private List<ProjectVO> filterByDerivedStatus(List<ProjectVO> projects, Project.ProjectStatus status) {
        if (status == null) {
            return projects;
        }
        Project.ProjectStatus normalizedStatus = status == Project.ProjectStatus.ARCHIVED
                ? Project.ProjectStatus.CLOSED
                : status;
        return projects.stream()
                .filter(project -> project.getProjectStatus() == normalizedStatus)
                .toList();
    }

    private Project.ProjectStatus resolveDisplayStatus(Project project, long acceptedCount, long pendingCount) {
        if (project.getProjectStatus() == Project.ProjectStatus.CLOSED
                || project.getProjectStatus() == Project.ProjectStatus.ARCHIVED) {
            return Project.ProjectStatus.CLOSED;
        }
        if (project.getMaxStudents() != null && acceptedCount >= project.getMaxStudents()) {
            return Project.ProjectStatus.CLOSED;
        }
        if (acceptedCount > 0) {
            return Project.ProjectStatus.AGREED;
        }
        if (pendingCount > 0) {
            return Project.ProjectStatus.REQUESTED;
        }
        return Project.ProjectStatus.AVAILABLE;
    }
}
