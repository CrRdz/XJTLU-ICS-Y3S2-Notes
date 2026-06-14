package com.cpt202.controller.teacher;

import com.cpt202.context.BaseContext;
import com.cpt202.dto.ProjectTagBindDTO;
import com.cpt202.result.Result;
import com.cpt202.service.ProjectService;
import com.cpt202.service.ProjectTagService;
import com.cpt202.vo.ProjectTagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教师端项目标签接口控制器。
 * 用于查询项目标签与执行项目标签重绑定。
 */
@RestController
@RequestMapping("/api/teacher/project-tags")
@Tag(name = "Teacher Project Tag API")
@Slf4j
public class TeacherProjectTagController {

    private final ProjectTagService projectTagService;
    private final ProjectService projectService;

    public TeacherProjectTagController(ProjectTagService projectTagService,
                                       ProjectService projectService) {
        this.projectTagService = projectTagService;
        this.projectService = projectService;
    }

    /**
     * 查询项目已绑定标签。
     *
     * @param projectId 项目主键
     * @return 项目标签展示对象列表
     */
    @GetMapping("/{projectId}")
    @Operation(summary = "List project tags")
    public Result<List<ProjectTagVO>> listProjectTags(@PathVariable Long projectId) {
        projectService.getOwnedProject(projectId, BaseContext.getCurrentUserId());
        log.info("List project tags: {}", projectId);
        return Result.success(projectTagService.listProjectTags(projectId));
    }

    /**
     * 为项目重新绑定标签。
     *
     * @param projectId 项目主键
     * @param bindDTO 标签绑定参数
     * @return 统一成功响应
     */
    @PutMapping("/{projectId}")
    @Operation(summary = "Bind tags to a project")
    public Result<Void> bindProjectTags(@PathVariable Long projectId,
                                        @Valid @RequestBody ProjectTagBindDTO bindDTO) {
        log.info("Bind project tags, projectId: {}, teacherId: {}, tagIds: {}",
                projectId, BaseContext.getCurrentUserId(), bindDTO.getTagIds());
        projectTagService.bindProjectTags(projectId, BaseContext.getCurrentUserId(), bindDTO.getTagIds());
        return Result.success();
    }
}
