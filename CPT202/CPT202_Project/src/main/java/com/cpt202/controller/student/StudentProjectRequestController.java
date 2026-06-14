package com.cpt202.controller.student;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.StudentProjectRequestQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.StudentRequestSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生端项目申请接口控制器。
 * 提供学生查询申请、提交申请以及撤回申请的入口。
 */
@RestController
@RequestMapping("/api/student/requests")
@Tag(name = "Student Request API")
public class StudentProjectRequestController {

    private final ProjectRequestService projectRequestService;

    public StudentProjectRequestController(ProjectRequestService projectRequestService) {
        this.projectRequestService = projectRequestService;
    }

    /**
     * 查询学生本人申请列表。
     *
     * @param queryDTO 学生申请查询参数
     * @return 申请展示对象列表
     */
    @GetMapping
    @Operation(summary = "List student requests")
    public Result<PageResult<ProjectRequestVO>> list(@Valid StudentProjectRequestQueryDTO queryDTO) {
        return Result.success(projectRequestService.listStudentRequestsPage(BaseContext.getCurrentUserId(), queryDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "List student requests by page")
    public Result<PageResult<ProjectRequestVO>> listPage(@Valid StudentProjectRequestQueryDTO queryDTO) {
        return Result.success(projectRequestService.listStudentRequestsPage(BaseContext.getCurrentUserId(), queryDTO));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get student request summary")
    public Result<StudentRequestSummaryVO> summary() {
        return Result.success(projectRequestService.getStudentRequestSummary(BaseContext.getCurrentUserId()));
    }

    @GetMapping("/context")
    @Operation(summary = "Get student request context for a project")
    public Result<List<ProjectRequestVO>> context(@RequestParam Long projectId) {
        return Result.success(projectRequestService.getStudentRequestContext(BaseContext.getCurrentUserId(), projectId));
    }

    /**
     * 提交新的项目申请。
     *
     * @param projectRequestCreateDTO 申请创建参数
     * @return 统一成功响应
     */
    @PostMapping
    @Operation(summary = "Submit a project request")
    public Result<Void> create(@Valid @RequestBody ProjectRequestCreateDTO projectRequestCreateDTO) {
        projectRequestService.create(BaseContext.getCurrentUserId(), projectRequestCreateDTO);
        return Result.success();
    }

    /**
     * 撤回指定申请。
     *
     * @param requestId 申请主键
     * @return 统一成功响应
     */
    @PutMapping("/{requestId}/withdraw")
    @Operation(summary = "Withdraw a project request")
    public Result<Void> withdraw(@PathVariable Long requestId) {
        projectRequestService.withdraw(requestId, BaseContext.getCurrentUserId());
        return Result.success();
    }
}
