package com.cpt202.controller.admin;

import com.cpt202.dto.CategoryDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.CategoryService;
import com.cpt202.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin category interface controller.
 * Responsible for handling frontend requests for querying and maintaining category resources.
 * Query interfaces return data, while create, update, and delete interfaces only return the operation result.
 */
@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "Admin Category API")
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * List all categories.
     *
     * @return A list of category view objects (VOs)
     */
    @GetMapping
    @Operation(summary = "List categories")
    public Result<List<CategoryVO>> list() {
        log.info("List categories");
        return Result.success(categoryService.listAll());
    }

    @GetMapping("/page")
    @Operation(summary = "List categories by page")
    public Result<PageResult<CategoryVO>> listPage(@Valid PageQueryDTO queryDTO) {
        log.info("List categories by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(categoryService.listPage(queryDTO));
    }

    /**
     * Get category details by primary key.
     *
     * @param categoryId Category primary key
     * @return Category view object (VO)
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by ID")
    public Result<CategoryVO> getById(@PathVariable Long categoryId) {
        log.info("Get category by id: {}", categoryId);
        return Result.success(categoryService.getById(categoryId));
    }

    /**
     * Create a new category.
     *
     * @param categoryDTO Category creation parameters
     * @return Unified success response
     */
    @PostMapping
    @Operation(summary = "Create a category")
    public Result<Void> create(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Create category: {}", categoryDTO);
        categoryService.create(categoryDTO);
        return Result.success();
    }

    /**
     * Update a specific category.
     *
     * @param categoryId Category primary key
     * @param categoryDTO Category update parameters
     * @return Unified success response
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "Update a category")
    public Result<Void> update(@PathVariable Long categoryId,
                               @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Update category: {}, payload: {}", categoryId, categoryDTO);
        categoryService.update(categoryId, categoryDTO);
        return Result.success();
    }

    /**
     * Delete a specific category.
     *
     * @param categoryId Category primary key
     * @return Unified success response
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete a category")
    public Result<Void> delete(@PathVariable Long categoryId) {
        log.info("Delete category: {}", categoryId);
        categoryService.delete(categoryId);
        return Result.success();
    }
}