package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.impl.ProjectRequestValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for request-validation and approval-side synchronization rules. */
@ExtendWith(MockitoExtension.class)
class ProjectRequestValidationServiceImplTest {

    @Mock
    private ProjectRequestRepository requestRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @InjectMocks
    private ProjectRequestValidationServiceImpl validationService;

    /** Rejects new requests for projects that are already closed. */
    @Test
    void validateRequestShouldRejectClosedProject() {
        Project project = project(1L, 2, Project.ProjectStatus.CLOSED);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);

            RuleViolationException exception = assertThrows(RuleViolationException.class,
                    () -> validationService.validateRequest(1L, project));

            assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_NOT_ACCEPTING_REQUESTS);
            verify(requestRepository, never()).existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(any(), any(), anyList());
        }
    }

    /** Rejects a second active request for the same student and project. */
    @Test
    void validateRequestShouldRejectDuplicatedRequestForSameProject() {
        Project project = project(2L, 2, Project.ProjectStatus.AVAILABLE);
        project.setCloseDate(LocalDateTime.of(2026, 5, 29, 23, 59));
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        when(requestRepository.existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(3L, 2L,
                List.of(ProjectRequest.RequestStatus.PENDING, ProjectRequest.RequestStatus.ACCEPTED)))
                .thenReturn(true);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);

            RuleViolationException exception = assertThrows(RuleViolationException.class,
                    () -> validationService.validateRequest(3L, project));

            assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_REQUEST_ALREADY_EXISTS);
        }
    }

    /** Rejects students who already hold an accepted project elsewhere. */
    @Test
    void validateRequestShouldRejectStudentWhoAlreadyHasAcceptedProject() {
        Project project = project(3L, 2, Project.ProjectStatus.AVAILABLE);
        project.setCloseDate(LocalDateTime.of(2026, 5, 29, 23, 59));
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        when(requestRepository.existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(eq(4L), eq(3L), anyList()))
                .thenReturn(false);
        when(requestRepository.existsByStudent_StudentIdAndRequestStatusIn(4L, List.of(ProjectRequest.RequestStatus.ACCEPTED)))
                .thenReturn(true);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);

            RuleViolationException exception = assertThrows(RuleViolationException.class,
                    () -> validationService.validateRequest(4L, project));

            assertThat(exception.getMessage()).isEqualTo(MessageConstants.ACTIVE_REQUEST_EXISTS);
        }
    }

    /** Rejects requests when the project's own application deadline has passed. */
    @Test
    void validateRequestShouldRejectExpiredProjectDeadline() {
        Project project = project(4L, 2, Project.ProjectStatus.AVAILABLE);
        project.setCloseDate(LocalDateTime.of(2026, 4, 30, 23, 59));
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);

            RuleViolationException exception = assertThrows(RuleViolationException.class,
                    () -> validationService.validateRequest(5L, project));

            assertThat(exception.getMessage()).isEqualTo(MessageConstants.REQUEST_DEADLINE_PASSED);
            verify(requestRepository, never()).existsByStudent_StudentIdAndProject_ProjectIdAndRequestStatusIn(any(), any(), anyList());
        }
    }

    /** Rejects other pending requests and closes the project when capacity is full. */
    @Test
    @SuppressWarnings("unchecked")
    void onApprovalSuccessShouldRejectOtherPendingRequestsAndCloseFullProject() {
        Long requestId = 5L;
        Long studentId = 11L;
        Long projectId = 21L;
        StudentProfile student = student(studentId);
        Project project = project(projectId, 1, Project.ProjectStatus.REQUESTED);
        ProjectRequest approvedRequest = request(requestId, student, project, ProjectRequest.RequestStatus.ACCEPTED);
        ProjectRequest otherPending = request(6L, student, project(22L, 2, Project.ProjectStatus.REQUESTED), ProjectRequest.RequestStatus.PENDING);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(approvedRequest));
        when(requestRepository.findByStudent_StudentIdAndRequestStatus(studentId, ProjectRequest.RequestStatus.PENDING))
                .thenReturn(List.of(approvedRequest, otherPending));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(requestRepository.countByProject_ProjectIdAndRequestStatus(projectId, ProjectRequest.RequestStatus.ACCEPTED))
                .thenReturn(1L);

        validationService.onApprovalSuccess(requestId);

        ArgumentCaptor<List<ProjectRequest>> requestsCaptor = ArgumentCaptor.forClass((Class<List<ProjectRequest>>) (Class<?>) List.class);
        ArgumentCaptor<RequestStatusHistory> historyCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(requestRepository).saveAll(requestsCaptor.capture());
        verify(requestStatusHistoryRepository).save(historyCaptor.capture());
        verify(projectRepository).save(projectCaptor.capture());

        List<ProjectRequest> savedRequests = requestsCaptor.getValue();
        assertThat(savedRequests).hasSize(2);
        assertThat(savedRequests.get(1).getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED);
        assertThat(savedRequests.get(1).getDecisionComment()).isEqualTo("System auto-rejected: Already matched elsewhere.");
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED.name());
        assertThat(historyCaptor.getValue().getRemark()).isEqualTo("System auto-rejected: The student has been accepted into another project.");

        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
        assertThat(savedProject.getCurrentAgreedCount()).isEqualTo(1);
        assertThat(savedProject.getCloseDate()).isNotNull();
    }

    /** Marks the project as agreed when capacity still remains after approval. */
    @Test
    void onApprovalSuccessShouldMarkProjectAsAgreedWhenCapacityRemains() {
        Long requestId = 7L;
        Long studentId = 12L;
        Long projectId = 23L;
        StudentProfile student = student(studentId);
        Project project = project(projectId, 3, Project.ProjectStatus.REQUESTED);
        ProjectRequest approvedRequest = request(requestId, student, project, ProjectRequest.RequestStatus.ACCEPTED);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(approvedRequest));
        when(requestRepository.findByStudent_StudentIdAndRequestStatus(studentId, ProjectRequest.RequestStatus.PENDING))
                .thenReturn(List.of(approvedRequest));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(requestRepository.countByProject_ProjectIdAndRequestStatus(projectId, ProjectRequest.RequestStatus.ACCEPTED))
                .thenReturn(1L);

        validationService.onApprovalSuccess(requestId);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.AGREED);
        assertThat(savedProject.getCloseDate()).isNull();
    }

    private StudentProfile student(Long studentId) {
        StudentProfile student = new StudentProfile();
        ReflectionTestUtils.setField(student, "studentId", studentId);
        return student;
    }

    private Project project(Long projectId, int maxStudents, Project.ProjectStatus status) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "projectId", projectId);
        project.setMaxStudents(maxStudents);
        project.setProjectStatus(status);
        return project;
    }

    private ProjectRequest request(Long requestId,
                                   StudentProfile student,
                                   Project project,
                                   ProjectRequest.RequestStatus status) {
        ProjectRequest request = new ProjectRequest();
        ReflectionTestUtils.setField(request, "requestId", requestId);
        request.setStudent(student);
        request.setProject(project);
        request.setRequestStatus(status);
        return request;
    }
}
