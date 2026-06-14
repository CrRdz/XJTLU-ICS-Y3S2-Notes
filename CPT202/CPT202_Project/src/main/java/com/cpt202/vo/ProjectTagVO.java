package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目标签展示对象。
 * <p>
 * 用于返回项目与标签之间的绑定结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTagVO {

    /** 项目主键。 */
    private Long projectId;
    /** 标签主键。 */
    private Long tagId;
    /** 标签名称。 */
    private String tagName;
}
