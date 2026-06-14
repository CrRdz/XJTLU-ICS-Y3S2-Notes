package com.cpt202.service;

import com.cpt202.vo.RequestStatusHistoryVO;

import java.util.List;

/**
 * 历史记录服务接口。
 * 负责提供申请状态历史的查询能力。
 */
public interface HistoryService {

    /**
     * 查询申请状态历史。
     *
     * @param requestId 申请主键
     * @return 申请状态历史列表
     */
    List<RequestStatusHistoryVO> getRequestHistory(Long requestId, Long studentId);

}
