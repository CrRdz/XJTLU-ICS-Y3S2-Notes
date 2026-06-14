package com.cpt202.controller.teacher;

import com.cpt202.result.Result;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Teacher-facing read-only category endpoints.
 */
@RestController
@RequestMapping("/api/teacher/categories")
@Tag(name = "Teacher Category API")
@Slf4j
public class TeacherCategoryController {

    private final CategoryService categoryService;

    public TeacherCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List categories")
    public Result<List<CategoryVO>> list() {
        log.info("Teacher list categories");
        return Result.success(categoryService.listAll());
    }
}
