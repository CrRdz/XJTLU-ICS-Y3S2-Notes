package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目填充率展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFillRateVO {

    /** 项目名称。 */
    private String name;
    /** 当前已录取人数。 */
    private Integer current;
    /** 项目容量。 */
    private Integer max;
    /** 填充率百分比。 */
    private Integer rate;
}