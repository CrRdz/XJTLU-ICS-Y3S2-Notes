package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 学生端请求摘要。
 * <p>
 * 用于 dashboard 等页面读取聚合后的申请统计和最近记录，
 * 避免前端为展示少量信息拉取整张申请表。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRequestSummaryVO {

    private long totalRequests;
    private long pendingCount;
    private long acceptedCount;
    private long rejectedCount;
    private long withdrawnCount;
    private List<Long> withdrawnProjectIds;
    private List<ProjectRequestVO> recentRequests;
}