package com.cpt202.controller.teacher;

import com.cpt202.result.Result;
import com.cpt202.service.CategoryService;
import com.cpt202.service.TagService;
import com.cpt202.vo.CategoryVO;
import com.cpt202.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教师端项目元数据接口。
 */
@RestController
@RequestMapping("/api/teacher/meta")
@RequiredArgsConstructor
@Tag(name = "Teacher Meta API")
public class TeacherMetaController {

    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping("/categories")
    @Operation(summary = "List categories for teachers")
    public Result<List<CategoryVO>> listCategories() {
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/tags")
    @Operation(summary = "List tags for teachers")
    public Result<List<TagVO>> listTags() {
        return Result.success(tagService.listAll());
    }
}
