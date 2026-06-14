package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.impl.HistoryServiceImpl;
import com.cpt202.vo.RequestStatusHistoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Unit tests for student-visible request history access rules. */
@ExtendWith(MockitoExtension.class)
class HistoryServiceImplTest {

    @Mock
    private ProjectRequestRepository projectRequestRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @InjectMocks
    private HistoryServiceImpl historyService;

    /** Rejects history lookups for requests owned by another student. */
    @Test
    void getRequestHistoryShouldRejectOtherStudentsRequest() {
        StudentProfile owner = student(1L, "Owner Student");
        ProjectRequest request = request(11L, owner);

        when(projectRequestRepository.findById(11L)).thenReturn(Optional.of(request));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> historyService.getRequestHistory(11L, 99L));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CANNOT_VIEW_OTHER_STUDENT_HISTORY);
    }

    /** Maps request history entries with student identity and status changes. */
    @Test
    void getRequestHistoryShouldMapStudentIdentityAndStatuses() {
        StudentProfile student = student(2L, "Student Two");
        ProjectRequest request = request(12L, student);
        RequestStatusHistory submitted = history("PENDING", "Student submitted the request.", student);
        RequestStatusHistory withdrawn = history("WITHDRAWN", "Student withdrew the request.", student);

        when(projectRequestRepository.findById(12L)).thenReturn(Optional.of(request));
        when(requestStatusHistoryRepository.findByRequest_RequestIdOrderByChangedAtAsc(12L))
                .thenReturn(List.of(submitted, withdrawn));

        List<RequestStatusHistoryVO> results = historyService.getRequestHistory(12L, 2L);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getRequestId()).isEqualTo(12L);
        assertThat(results.get(0).getNewStatus()).isEqualTo("PENDING");
        assertThat(results.get(0).getChangedByStudentId()).isEqualTo(2L);
        assertThat(results.get(0).getChangedByStudentName()).isEqualTo("Student Two");
        assertThat(results.get(1).getNewStatus()).isEqualTo("WITHDRAWN");
        assertThat(results.get(1).getRemark()).isEqualTo("Student withdrew the request.");
    }

    private ProjectRequest request(Long requestId, StudentProfile student) {
        ProjectRequest request = new ProjectRequest();
        ReflectionTestUtils.setField(request, "requestId", requestId);
        request.setStudent(student);
        return request;
    }

    private StudentProfile student(Long studentId, String fullName) {
        StudentProfile student = new StudentProfile();
        ReflectionTestUtils.setField(student, "studentId", studentId);
        User user = new User();
        user.setUserId(studentId);
        user.setFullName(fullName);
        student.setUser(user);
        return student;
    }

    private RequestStatusHistory history(String newStatus, String remark, StudentProfile changedBy) {
        RequestStatusHistory history = new RequestStatusHistory();
        history.setNewStatus(newStatus);
        history.setRemark(remark);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        return history;
    }
}