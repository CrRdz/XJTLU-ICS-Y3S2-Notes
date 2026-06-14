package com.cpt202.controller.admin;

import com.cpt202.dto.TagDTO;
import com.cpt202.dto.PageQueryDTO;
import com.cpt202.result.PageResult;
import com.cpt202.result.Result;
import com.cpt202.service.TagService;
import com.cpt202.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin tag interface controller.
 * Responsible for querying, creating, updating, and deleting tags.
 */
@RestController
@RequestMapping("/api/admin/tags")
@Tag(name = "Admin Tag API")
@Slf4j
public class AdminTagController {

    private final TagService tagService;

    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * List all tags.
     *
     * @return A list of tag view objects (VOs)
     */
    @GetMapping
    @Operation(summary = "List tags")
    public Result<List<TagVO>> list() {
        log.info("List tags");
        return Result.success(tagService.listAll());
    }

    @GetMapping("/page")
    @Operation(summary = "List tags by page")
    public Result<PageResult<TagVO>> listPage(@Valid PageQueryDTO queryDTO) {
        log.info("List tags by page, pageNum: {}, pageSize: {}", queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(tagService.listPage(queryDTO));
    }

    /**
     * Get tag details by primary key.
     *
     * @param tagId Tag primary key
     * @return Tag view object (VO)
     */
    @GetMapping("/{tagId}")
    @Operation(summary = "Get tag by ID")
    public Result<TagVO> getById(@PathVariable Long tagId) {
        log.info("Get tag by id: {}", tagId);
        return Result.success(tagService.getById(tagId));
    }

    /**
     * Create a new tag.
     *
     * @param tagDTO Tag creation parameters
     * @return Unified success response
     */
    @PostMapping
    @Operation(summary = "Create a tag")
    public Result<Void> create(@Valid @RequestBody TagDTO tagDTO) {
        log.info("Create tag: {}", tagDTO);
        tagService.create(tagDTO);
        return Result.success();
    }

    /**
     * Update a tag.
     *
     * @param tagId Tag primary key
     * @param tagDTO Tag update parameters
     * @return Unified success response
     */
    @PutMapping("/{tagId}")
    @Operation(summary = "Update a tag")
    public Result<Void> update(@PathVariable Long tagId,
                               @Valid @RequestBody TagDTO tagDTO) {
        log.info("Update tag: {}, payload: {}", tagId, tagDTO);
        tagService.update(tagId, tagDTO);
        return Result.success();
    }

    /**
     * Delete a tag.
     *
     * @param tagId Tag primary key
     * @return Unified success response
     */
    @DeleteMapping("/{tagId}")
    @Operation(summary = "Delete a tag")
    public Result<Void> delete(@PathVariable Long tagId) {
        log.info("Delete tag: {}", tagId);
        tagService.delete(tagId);
        return Result.success();
    }
}