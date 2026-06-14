package com.cpt202.unit.validation.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.validation.impl.ProjectValidationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Unit tests for project-domain validation rules. */
@ExtendWith(MockitoExtension.class)
class ProjectValidationServiceImplTest {

    @InjectMocks
    private ProjectValidationServiceImpl projectValidationService;

    private static TeacherProfile teacher(Long id) {
        TeacherProfile t = mock(TeacherProfile.class);
        when(t.getTeacherId()).thenReturn(id);
        return t;
    }

    private static Project project(Project.ProjectStatus status, TeacherProfile teacher) {
        Project p = new Project();
        p.setProjectStatus(status);
        p.setTeacher(teacher);
        return p;
    }

    /* ---- checkManualStatusChange ---- */

    @Test
    void checkManualStatusChangeShouldPassForCloseTransition() {
        assertDoesNotThrow(() -> projectValidationService.checkManualStatusChange(
                project(Project.ProjectStatus.AGREED, null), Project.ProjectStatus.CLOSED));
    }

    @Test
    void checkManualStatusChangeShouldRejectManualRequested() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectValidationService.checkManualStatusChange(
                        project(Project.ProjectStatus.AVAILABLE, null), Project.ProjectStatus.REQUESTED));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY);
    }

    @Test
    void checkManualStatusChangeShouldRejectArchived() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectValidationService.checkManualStatusChange(
                        project(Project.ProjectStatus.CLOSED, null), Project.ProjectStatus.ARCHIVED));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_ARCHIVED_DISABLED);
    }

    @Test
    void checkManualStatusChangeShouldRejectReopenClosedProject() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectValidationService.checkManualStatusChange(
                        project(Project.ProjectStatus.CLOSED, null), Project.ProjectStatus.AVAILABLE));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.PROJECT_STATUS_TRANSITION_INVALID);
    }

    @Test
    void checkManualStatusChangeShouldAllowCloseOfClosedProject() {
        assertDoesNotThrow(() -> projectValidationService.checkManualStatusChange(
                project(Project.ProjectStatus.CLOSED, null), Project.ProjectStatus.CLOSED));
    }

    @Test
    void checkProjectCloseDateShouldPassForFutureDate() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);
            assertDoesNotThrow(() -> projectValidationService.checkProjectCloseDate(fixedNow.plusDays(1)));
        }
    }

    @Test
    void checkProjectCloseDateShouldRejectPastDate() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 1, 12, 0);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> projectValidationService.checkProjectCloseDate(fixedNow.minusMinutes(1)));
            assertThat(ex.getMessage()).isEqualTo(MessageConstants.PROJECT_CLOSE_DATE_INVALID);
        }
    }

    /* ---- checkProjectOwnership ---- */

    @Test
    void checkProjectOwnershipShouldPassForOwner() {
        Project p = project(Project.ProjectStatus.AVAILABLE, teacher(1L));
        assertDoesNotThrow(() -> projectValidationService.checkProjectOwnership(p, 1L));
    }

    @Test
    void checkProjectOwnershipShouldRejectNonOwner() {
        Project p = project(Project.ProjectStatus.AVAILABLE, teacher(1L));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectValidationService.checkProjectOwnership(p, 2L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.CANNOT_OPERATE_OTHER_TEACHER_PROJECT);
    }

    @Test
    void checkProjectOwnershipShouldRejectNullTeacher() {
        Project p = project(Project.ProjectStatus.AVAILABLE, null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> projectValidationService.checkProjectOwnership(p, 1L));
        assertThat(ex.getMessage()).isEqualTo(MessageConstants.CANNOT_OPERATE_OTHER_TEACHER_PROJECT);
    }
}
