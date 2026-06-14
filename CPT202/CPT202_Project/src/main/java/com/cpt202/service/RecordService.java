package com.cpt202.service;

import com.cpt202.dto.AdminRequestRecordQueryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.result.PageResult;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;

import java.util.List;

/**
 * 管理端记录服务接口。
 * <p>
 * 用于查询项目记录、申请记录以及申请历史记录。
 */
public interface RecordService {

    /**
     * 查询项目记录列表。
     *
     * @return 项目展示对象列表
     */
    List<ProjectVO> listProjectRecords();

    /**
     * 分页查询项目记录列表。
     *
     * @param queryDTO 分页查询参数
     * @return 项目分页结果
     */
    PageResult<ProjectVO> listProjectRecordsPage(PageQueryDTO queryDTO);

    /**
     * 查询申请记录列表。
     *
     * @param status 申请状态筛选条件
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listRequestRecords(ProjectRequest.RequestStatus status);

    /**
     * 分页查询申请记录列表。
     *
     * @param queryDTO 申请记录分页查询参数
     * @return 申请记录分页结果
     */
    PageResult<ProjectRequestVO> listRequestRecordsPage(AdminRequestRecordQueryDTO queryDTO);

    /**
     * 查询申请历史记录列表。
     *
     * @return 申请状态历史列表
     */
    List<RequestStatusHistoryVO> listRequestHistoryRecords();

    /**
     * 分页查询申请状态历史列表。
     *
     * @param queryDTO 分页查询参数
     * @return 历史记录分页结果
     */
    PageResult<RequestStatusHistoryVO> listRequestHistoryRecordsPage(PageQueryDTO queryDTO);
}
