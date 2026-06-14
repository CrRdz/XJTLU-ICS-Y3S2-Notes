package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图表标签-数值展示对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartCountVO {

    /** 图表标签。 */
    private String label;
    /** 对应数量。 */
    private Long value;
}