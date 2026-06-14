package com.cpt202.controller.student;

import com.cpt202.context.BaseContext;
import com.cpt202.result.Result;
import com.cpt202.service.HistoryService;
import com.cpt202.vo.RequestStatusHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端申请历史接口控制器。
 * 用于查看申请状态流转历史，便于学生理解申请进展。
 */
@RestController
@RequestMapping("/api/student/request-history")
@Tag(name = "Student Request History API")
@Slf4j
public class StudentRequestHistoryController {

    private final HistoryService historyService;

    public StudentRequestHistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * 根据申请主键查询其状态历史记录。
     *
     * @param requestId 申请主键
     * @return 申请状态历史列表
     */
    @GetMapping("/{requestId}")
    @Operation(summary = "Get request status history")
    public Result<List<RequestStatusHistoryVO>> getRequestHistory(@PathVariable Long requestId) {
        log.info("Get request history: {}", requestId);
        return Result.success(historyService.getRequestHistory(requestId, BaseContext.getCurrentUserId()));
    }
}
