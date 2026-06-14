package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.RequestStatusHistory;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.RequestStatusHistoryRepository;
import com.cpt202.service.HistoryService;
import com.cpt202.util.VoConverter;
import com.cpt202.vo.RequestStatusHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 历史记录服务实现类。
 * 负责申请状态历史的读取与转换。
 */
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final ProjectRequestRepository projectRequestRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    /**
     * 查询申请状态历史。
     *
     * @param requestId 申请主键
     * @return 申请状态历史列表
     */
    @Override
    public List<RequestStatusHistoryVO> getRequestHistory(Long requestId, Long studentId) {
        ProjectRequest request = projectRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.REQUEST_NOT_FOUND));

        if (request.getStudent() == null || !studentId.equals(request.getStudent().getStudentId())) {
            throw new BusinessException(MessageConstants.CANNOT_VIEW_OTHER_STUDENT_HISTORY);
        }

        List<RequestStatusHistory> histories = requestStatusHistoryRepository.findByRequest_RequestIdOrderByChangedAtAsc(requestId);

        return VoConverter.toList(histories,
                h -> toRequestStatusHistoryVO(h, requestId));
    }

    private RequestStatusHistoryVO toRequestStatusHistoryVO(RequestStatusHistory history, Long requestId) {
        RequestStatusHistoryVO historyVO = new RequestStatusHistoryVO();

        BeanUtils.copyProperties(history, historyVO);
        historyVO.setRequestId(requestId);
        historyVO.setChangedByStudentId(history.getChangedBy() == null ? null : history.getChangedBy().getStudentId());
        historyVO.setChangedByStudentName(history.getChangedBy() == null || history.getChangedBy().getUser() == null
                ? null : history.getChangedBy().getUser().getFullName());

        return historyVO;
    }

}
