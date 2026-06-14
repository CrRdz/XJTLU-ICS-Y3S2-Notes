package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.service.ProjectRequestValidationService;
import com.cpt202.service.impl.ProjectRequestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for request creation, review, and withdrawal service rules. */
@ExtendWith(MockitoExtension.class)
class ProjectRequestServiceImplTest {

    @Mock
    private ProjectRequestRepository requestRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private StudentProfileRepository studentRepository;

    @Mock
    private TeacherProfileRepository teacherProfileRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @Mock
    private ProjectRequestValidationService projectRequestValidationService;

    @InjectMocks
    private ProjectRequestServiceImpl projectRequestService;

    /** Rejects request creation when the student reuses an active preference rank. */
    @Test
    void createShouldRejectDuplicatedPreferenceRank() {
        Long studentId = 1L;
        Long projectId = 10L;
        StudentProfile student = studentProfile(studentId, user(101L, User.UserRole.STUDENT));
        Project project = project(projectId, teacherProfile(20L, user(201L, User.UserRole.TEACHER)), 2, Project.ProjectStatus.AVAILABLE);
        ProjectRequestCreateDTO dto = createDTO(projectId, 1, "apply");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(requestRepository.existsByStudent_StudentIdAndPreferenceRankAndRequestStatusIn(
                eq(studentId), eq(1), any()))
                .thenReturn(true);

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> projectRequestService.create(studentId, dto));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_PREFERENCE_RANK_DUPLICATED);
        verify(requestRepository, never()).save(any(ProjectRequest.class));
    }

    /** Rejects reviews attempted by a teacher who does not own the request. */
    @Test
    void reviewShouldRejectOtherTeacherRequest() {
        Long requestId = 30L;
        TeacherProfile ownerTeacher = teacherProfile(2L, user(202L, User.UserRole.TEACHER));
        StudentProfile student = studentProfile(5L, user(105L, User.UserRole.STUDENT));
        ProjectRequest request = request(requestId,
                project(11L, ownerTeacher, 1, Project.ProjectStatus.REQUESTED),
                student,
                ProjectRequest.RequestStatus.PENDING);
        ProjectRequestReviewDTO reviewDTO = reviewDTO(ProjectRequest.RequestStatus.REJECTED, "not your request");

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectRequestService.review(requestId, 99L, reviewDTO));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CANNOT_REVIEW_OTHER_TEACHER_REQUEST);
        verify(teacherProfileRepository, never()).findById(any());
    }

    /** Rejects review when the request is no longer pending. */
    @Test
    void reviewShouldRejectWhenRequestIsNotPending() {
        Long teacherId = 7L;
        TeacherProfile teacher = teacherProfile(teacherId, user(207L, User.UserRole.TEACHER));
        StudentProfile student = studentProfile(5L, user(105L, User.UserRole.STUDENT));
        ProjectRequest request = request(31L,
                project(12L, teacher, 2, Project.ProjectStatus.AGREED),
                student,
                ProjectRequest.RequestStatus.ACCEPTED);
        ProjectRequestReviewDTO reviewDTO = reviewDTO(ProjectRequest.RequestStatus.REJECTED, "duplicate review");

        when(requestRepository.findById(31L)).thenReturn(Optional.of(request));
        when(teacherProfileRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> projectRequestService.review(31L, teacherId, reviewDTO));

        assertThat(exception.getMessage()).contains("not in pending status");
        verify(requestRepository, never()).save(any(ProjectRequest.class));
    }

    /** Rejects acceptance when the project has already reached capacity. */
    @Test
    void reviewShouldRejectAcceptedWhenProjectIsFull() {
        Long teacherId = 8L;
        TeacherProfile teacher = teacherProfile(teacherId, user(208L, User.UserRole.TEACHER));
        StudentProfile student = studentProfile(6L, user(106L, User.UserRole.STUDENT));
        Project project = project(13L, teacher, 1, Project.ProjectStatus.REQUESTED);
        ProjectRequest request = request(32L, project, student, ProjectRequest.RequestStatus.PENDING);
        ProjectRequestReviewDTO reviewDTO = reviewDTO(ProjectRequest.RequestStatus.ACCEPTED, "capacity reached");

        when(requestRepository.findById(32L)).thenReturn(Optional.of(request));
        when(teacherProfileRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(requestRepository.countByProject_ProjectIdAndRequestStatus(project.getProjectId(), ProjectRequest.RequestStatus.ACCEPTED))
                .thenReturn(1L);

        RuleViolationException exception = assertThrows(RuleViolationException.class,
                () -> projectRequestService.review(32L, teacherId, reviewDTO));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PROJECT_CAPACITY_EXCEEDED);
        verify(requestRepository, never()).save(any(ProjectRequest.class));
    }

    /** Saves the reviewed request and delegates follow-up approval handling. */
    @Test
    void reviewShouldSaveAndDelegateOnApprovalSuccess() {
        Long teacherId = 9L;
        TeacherProfile teacher = teacherProfile(teacherId, user(209L, User.UserRole.TEACHER));
        StudentProfile student = studentProfile(7L, user(107L, User.UserRole.STUDENT));
        Project project = project(14L, teacher, 2, Project.ProjectStatus.REQUESTED);
        ProjectRequest request = request(33L, project, student, ProjectRequest.RequestStatus.PENDING);
        ProjectRequestReviewDTO reviewDTO = reviewDTO(ProjectRequest.RequestStatus.ACCEPTED, "accepted");

        when(requestRepository.findById(33L)).thenReturn(Optional.of(request));
        when(teacherProfileRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(requestRepository.countByProject_ProjectIdAndRequestStatus(project.getProjectId(), ProjectRequest.RequestStatus.ACCEPTED))
                .thenReturn(0L);

        projectRequestService.review(33L, teacherId, reviewDTO);

        ArgumentCaptor<ProjectRequest> requestCaptor = ArgumentCaptor.forClass(ProjectRequest.class);
        ArgumentCaptor<RequestStatusHistory> historyCaptor = ArgumentCaptor.forClass(RequestStatusHistory.class);
        verify(requestRepository).save(requestCaptor.capture());
        verify(requestStatusHistoryRepository).save(historyCaptor.capture());
        verify(projectRequestValidationService).onApprovalSuccess(33L);

        assertThat(requestCaptor.getValue().getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.ACCEPTED);
        assertThat(requestCaptor.getValue().getReviewedBy()).isEqualTo(teacher);
        assertThat(historyCaptor.getValue().getOldStatus()).isEqualTo(ProjectRequest.RequestStatus.PENDING.name());
        assertThat(historyCaptor.getValue().getNewStatus()).isEqualTo(ProjectRequest.RequestStatus.ACCEPTED.name());
        assertThat(historyCaptor.getValue().getRemark()).isEqualTo("accepted");
    }

    /** Rejects withdrawal when the request belongs to another student. */
    @Test
    void withdrawShouldRejectOtherStudentsRequest() {
        StudentProfile owner = studentProfile(3L, user(103L, User.UserRole.STUDENT));
        TeacherProfile teacher = teacherProfile(10L, user(210L, User.UserRole.TEACHER));
        ProjectRequest request = request(34L,
                project(15L, teacher, 2, Project.ProjectStatus.REQUESTED),
                owner,
                ProjectRequest.RequestStatus.PENDING);

        when(requestRepository.findById(34L)).thenReturn(Optional.of(request));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectRequestService.withdraw(34L, 99L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST);
        verify(requestRepository, never()).save(any(ProjectRequest.class));
    }

    private ProjectRequestCreateDTO createDTO(Long projectId, Integer preferenceRank, String notes) {
        ProjectRequestCreateDTO dto = new ProjectRequestCreateDTO();
        dto.setProjectId(projectId);
        dto.setPreferenceRank(preferenceRank);
        dto.setNotes(notes);
        return dto;
    }

    private ProjectRequestReviewDTO reviewDTO(ProjectRequest.RequestStatus status, String decisionComment) {
        ProjectRequestReviewDTO dto = new ProjectRequestReviewDTO();
        dto.setRequestStatus(status);
        dto.setDecisionComment(decisionComment);
        return dto;
    }

    private User user(Long userId, User.UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setFullName(role.name() + userId);
        return user;
    }

    private TeacherProfile teacherProfile(Long teacherId, User user) {
        TeacherProfile teacherProfile = new TeacherProfile();
        ReflectionTestUtils.setField(teacherProfile, "teacherId", teacherId);
        teacherProfile.setUser(user);
        return teacherProfile;
    }

    private StudentProfile studentProfile(Long studentId, User user) {
        StudentProfile studentProfile = new StudentProfile();
        ReflectionTestUtils.setField(studentProfile, "studentId", studentId);
        studentProfile.setUser(user);
        return studentProfile;
    }

    private Project project(Long projectId,
                            TeacherProfile teacherProfile,
                            int maxStudents,
                            Project.ProjectStatus status) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "projectId", projectId);
        project.setTeacher(teacherProfile);
        project.setMaxStudents(maxStudents);
        project.setProjectStatus(status);
        project.setUpdatedAt(LocalDateTime.now());
        return project;
    }

    private ProjectRequest request(Long requestId,
                                   Project project,
                                   StudentProfile studentProfile,
                                   ProjectRequest.RequestStatus status) {
        ProjectRequest request = new ProjectRequest();
        ReflectionTestUtils.setField(request, "requestId", requestId);
        request.setProject(project);
        request.setStudent(studentProfile);
        request.setRequestStatus(status);
        request.setSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        return request;
    }
}