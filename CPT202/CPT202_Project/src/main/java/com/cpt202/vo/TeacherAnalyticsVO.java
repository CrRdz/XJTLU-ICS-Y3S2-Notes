package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教师端分析统计结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAnalyticsVO {

    private Long totalProjects;
    private Long totalRequests;
    private Long pendingCount;
    private Long acceptedCount;
    private Long rejectedCount;
    private Long withdrawnCount;
    private Long totalCapacity;
    private Long filledSlots;
    private List<ChartCountVO> requestStatusCounts;
    private List<ChartCountVO> projectStatusCounts;
    private List<ChartCountVO> requestsPerProject;
    private List<ChartCountVO> programmeCounts;
    private List<ChartCountVO> preferenceRankCounts;
    private List<ProjectFillRateVO> fillRateTopProjects;
}