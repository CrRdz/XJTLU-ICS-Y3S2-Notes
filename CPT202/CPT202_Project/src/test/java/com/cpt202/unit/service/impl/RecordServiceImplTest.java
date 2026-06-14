package com.cpt202.unit.service.impl;

import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.impl.RecordServiceImpl;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for record-query service projections. */
@ExtendWith(MockitoExtension.class)
class RecordServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectRequestRepository projectRequestRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @InjectMocks
    private RecordServiceImpl recordService;

    /** Returns project records exactly as exposed by the repository projection. */
    @Test
    void listProjectRecordsShouldReturnRepositoryProjection() {
        ProjectVO project = ProjectVO.builder()
                .projectId(1L)
                .title("AI Project")
                .build();
        when(projectRepository.findAllProjectVos()).thenReturn(List.of(project));

        List<ProjectVO> results = recordService.listProjectRecords();

        assertThat(results).containsExactly(project);
        verify(projectRepository).findAllProjectVos();
    }

    /** Passes the requested status filter through to the request repository. */
    @Test
    void listRequestRecordsShouldPassStatusToRepository() {
        ProjectRequestVO request = ProjectRequestVO.builder()
                .requestId(2L)
                .requestStatus(ProjectRequest.RequestStatus.ACCEPTED)
                .build();
        when(projectRequestRepository.findRequestVos(ProjectRequest.RequestStatus.ACCEPTED)).thenReturn(List.of(request));

        List<ProjectRequestVO> results = recordService.listRequestRecords(ProjectRequest.RequestStatus.ACCEPTED);

        assertThat(results).containsExactly(request);
        verify(projectRequestRepository).findRequestVos(ProjectRequest.RequestStatus.ACCEPTED);
    }

    /** Returns ordered request-history projections from the repository. */
    @Test
    void listRequestHistoryRecordsShouldReturnOrderedHistoryProjection() {
        RequestStatusHistoryVO history = RequestStatusHistoryVO.builder()
                .historyId(3L)
                .requestId(2L)
                .newStatus("ACCEPTED")
                .changedAt(LocalDateTime.now())
                .build();
        when(requestStatusHistoryRepository.findAllHistoryVos()).thenReturn(List.of(history));

        List<RequestStatusHistoryVO> results = recordService.listRequestHistoryRecords();

        assertThat(results).containsExactly(history);
        verify(requestStatusHistoryRepository).findAllHistoryVos();
    }
}