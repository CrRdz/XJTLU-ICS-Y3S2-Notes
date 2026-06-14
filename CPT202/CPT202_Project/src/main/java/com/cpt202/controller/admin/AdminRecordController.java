package com.cpt202.controller.admin;

import com.cpt202.dto.AdminRequestRecordQueryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.RecordService;
import com.cpt202.vo.ProjectVO;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.RequestStatusHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Admin record interface controller.
 * Used for unified querying of project records, application records, and application history,
 * facilitating data review and operational analysis for the management side.
 */
@RestController
@RequestMapping("/api/admin/records")
@Tag(name = "Admin Record API")
@Slf4j
public class AdminRecordController {

    private final RecordService recordService;

    public AdminRecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/projects")
    @Operation(summary = "List project records")
    public Result<PageResult<ProjectVO>> listProjectRecords(@Valid PageQueryDTO queryDTO) {
        log.info("List project records, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listProjectRecordsPage(queryDTO));
    }

    @GetMapping("/projects/page")
    @Operation(summary = "List project records by page")
    public Result<PageResult<ProjectVO>> listProjectRecordsPage(@Valid PageQueryDTO queryDTO) {
        log.info("List project records by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listProjectRecordsPage(queryDTO));
    }

    @GetMapping("/requests")
    @Operation(summary = "List request records")
    public Result<PageResult<ProjectRequestVO>> listRequestRecords(@Valid AdminRequestRecordQueryDTO queryDTO) {
        log.info("List request records, status: {}, pageNum: {}, pageSize: {}",
                queryDTO.getStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestRecordsPage(queryDTO));
    }

    @GetMapping("/requests/page")
    @Operation(summary = "List request records by page")
    public Result<PageResult<ProjectRequestVO>> listRequestRecordsPage(@Valid AdminRequestRecordQueryDTO queryDTO) {
        log.info("List request records by page, status: {}, pageNum: {}, pageSize: {}",
                queryDTO.getStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestRecordsPage(queryDTO));
    }

    @GetMapping("/request-history")
    @Operation(summary = "List request history records")
    public Result<PageResult<RequestStatusHistoryVO>> listRequestHistoryRecords(@Valid PageQueryDTO queryDTO) {
        log.info("List request history records, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestHistoryRecordsPage(queryDTO));
    }

    @GetMapping("/request-history/page")
    @Operation(summary = "List request history records by page")
    public Result<PageResult<RequestStatusHistoryVO>> listRequestHistoryRecordsPage(@Valid PageQueryDTO queryDTO) {
        log.info("List request history records by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(recordService.listRequestHistoryRecordsPage(queryDTO));
    }
}
