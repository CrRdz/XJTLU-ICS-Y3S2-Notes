package com.cpt202.controller.teacher;

import com.cpt202.result.Result;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Teacher-facing read-only tag endpoints.
 */
@RestController
@RequestMapping("/api/teacher/tags")
@Tag(name = "Teacher Tag API")
@Slf4j
public class TeacherTagController {

    private final TagService tagService;

    public TeacherTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "List tags")
    public Result<List<TagVO>> list() {
        log.info("Teacher list tags");
        return Result.success(tagService.listAll());
    }
}
