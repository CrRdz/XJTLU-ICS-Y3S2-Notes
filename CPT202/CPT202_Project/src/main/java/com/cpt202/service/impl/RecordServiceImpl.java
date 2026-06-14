package com.cpt202.service.impl;

import com.cpt202.dto.AdminRequestRecordQueryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.result.PageResult;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端记录服务实现类。
 * 用于组装项目记录、申请记录和申请历史记录等管理端所需查询结果。
 */
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final ProjectRepository projectRepository;
    private final ProjectRequestRepository projectRequestRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    /**
     * 查询项目记录列表。
     *
     * @return 项目展示对象列表
     */
    @Override
    public List<ProjectVO> listProjectRecords() {
        return projectRepository.findAllProjectVos();
    }

    @Override
    public PageResult<ProjectVO> listProjectRecordsPage(PageQueryDTO queryDTO) {
        Page<ProjectVO> projectPage = projectRepository.findAllProjectVos(toPageable(queryDTO));
        return PageResult.fromPage(projectPage);
    }

    /**
     * 查询申请记录列表。
     *
     * @param status 申请状态筛选条件
     * @return 申请展示对象列表
     */
    @Override
    public List<ProjectRequestVO> listRequestRecords(ProjectRequest.RequestStatus status) {
        return projectRequestRepository.findRequestVos(status);
    }

    @Override
    public PageResult<ProjectRequestVO> listRequestRecordsPage(AdminRequestRecordQueryDTO queryDTO) {
        Page<ProjectRequestVO> requestPage = projectRequestRepository.findRequestVos(queryDTO.getStatus(), toPageable(queryDTO));
        return PageResult.fromPage(requestPage);
    }

    /**
     * 查询申请历史记录列表。
     *
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> listRequestHistoryRecords() {
        return requestStatusHistoryRepository.findAllHistoryVos();
    }

    @Override
    public PageResult<RequestStatusHistoryVO> listRequestHistoryRecordsPage(PageQueryDTO queryDTO) {
        Page<RequestStatusHistoryVO> historyPage = requestStatusHistoryRepository.findAllHistoryVos(toPageable(queryDTO));
        return PageResult.fromPage(historyPage);
    }

    private Pageable toPageable(PageQueryDTO queryDTO) {
        return PageRequest.of(
                Math.max(0, queryDTO.getPageNum() - 1),
                queryDTO.getPageSize());
    }
}
