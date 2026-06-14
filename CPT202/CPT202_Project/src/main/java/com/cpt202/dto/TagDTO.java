package com.cpt202.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签新增/修改参数。
 * <p>
 * 用于接收管理端对标签的创建与更新请求体。
 */
@Data
public class TagDTO {

    /** 标签名称。 */
    @NotBlank
    private String tagName;

    /** 标签说明。 */
    private String description;
}
