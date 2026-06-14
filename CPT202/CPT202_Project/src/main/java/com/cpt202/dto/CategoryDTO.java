package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类新增/修改参数。
 * <p>
 * 用于接收管理端对分类的创建与更新请求体。
 */
@Data
public class CategoryDTO {

    /** 分类名称，不能为空。 */
    @NotBlank
    private String categoryName;

    /** 分类说明，可选。 */
    private String description;
}
