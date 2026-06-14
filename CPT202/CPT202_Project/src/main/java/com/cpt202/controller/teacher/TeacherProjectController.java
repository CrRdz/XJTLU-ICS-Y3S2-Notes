package com.cpt202.controller.teacher;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.TeacherProjectQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.ProjectService;
import com.cpt202.vo.ProjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 教师端项目接口控制器。
 * 教师可以通过该控制器查询本人项目、查看项目详情，
 * 并执行新增、修改和状态变更等维护操作。
 */
@RestController
@RequestMapping("/api/teacher/projects")
@Tag(name = "Teacher Project API")
public class TeacherProjectController {

    private final ProjectService projectService;

    public TeacherProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 查询教师名下项目列表。
     *
     * @param queryDTO 教师项目查询参数
     * @return 项目展示对象列表
     */
    @GetMapping
    @Operation(summary = "List teacher projects")
    public Result<PageResult<ProjectVO>> list(@Valid TeacherProjectQueryDTO queryDTO) {
        return Result.success(projectService.listTeacherProjectsPage(BaseContext.getCurrentUserId(), queryDTO));
    }

    @GetMapping("/page")
    @Operation(summary = "List teacher projects by page")
    public Result<PageResult<ProjectVO>> listPage(@Valid TeacherProjectQueryDTO queryDTO) {
        return Result.success(projectService.listTeacherProjectsPage(BaseContext.getCurrentUserId(), queryDTO));
    }

    /**
     * 查询项目详情。
     *
     * @param projectId 项目主键
     * @return 项目展示对象
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "Get teacher project details")
    public Result<ProjectVO> getById(@PathVariable Long projectId) {
        return Result.success(projectService.getOwnedProject(projectId, BaseContext.getCurrentUserId()));
    }

    /**
     * 新增项目。
     *
     * @param projectDTO 项目新增参数
     * @return 新增后的项目展示对象
     */
    @PostMapping
    @Operation(summary = "Create a project")
    public Result<ProjectVO> create(@Valid @RequestBody ProjectDTO projectDTO) {
        return Result.success(projectService.create(BaseContext.getCurrentUserId(), projectDTO));
    }

    /**
     * 修改项目。
     *
     * @param projectId 项目主键
     * @param projectDTO 项目更新参数
     * @return 统一成功响应
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Update a project")
    public Result<Void> update(@PathVariable Long projectId,
                               @Valid @RequestBody ProjectDTO projectDTO) {
        projectService.update(projectId, BaseContext.getCurrentUserId(), projectDTO);
        return Result.success();
    }

    /**
     * 修改项目状态。
     *
     * @param projectId 项目主键
     * @param projectStatusUpdateDTO 状态修改参数
     * @return 统一成功响应
     */
    @PutMapping("/{projectId}/status")
    @Operation(summary = "Change project status")
    public Result<Void> changeStatus(@PathVariable Long projectId,
                                     @Valid @RequestBody ProjectStatusUpdateDTO projectStatusUpdateDTO) {
        projectService.changeStatus(projectId, BaseContext.getCurrentUserId(), projectStatusUpdateDTO);
        return Result.success();
    }
}
