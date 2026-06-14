package com.cpt202.controller.admin;

import com.cpt202.result.Result;
import com.cpt202.service.ProjectTagService;
import com.cpt202.vo.ProjectTagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin project interface controller.
 * Manages project-related resources, such as project tags and details.
 */
@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
@Slf4j
public class AdminProjectController {

    private final ProjectTagService projectTagService;

    @GetMapping("/{projectId}/tags")
    public Result<List<ProjectTagVO>> getProjectTags(@PathVariable Long projectId) {
        log.info("Get tags of a project, projectId={}", projectId);
        List<ProjectTagVO> tags = projectTagService.listProjectTags(projectId);
        return Result.success(tags);
    }
}
