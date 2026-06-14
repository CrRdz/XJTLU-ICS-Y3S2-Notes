package com.cpt202.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页查询参数。
 */
@Data
public class PageQueryDTO {

    @Min(value = 1, message = "pageNum must be >= 1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize must be >= 1")
    @Max(value = 50, message = "pageSize must be <= 50")
    private Integer pageSize = 10;
}