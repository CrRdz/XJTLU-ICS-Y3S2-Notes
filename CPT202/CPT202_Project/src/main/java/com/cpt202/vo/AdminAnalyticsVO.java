package com.cpt202.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端分析统计结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsVO {

    private Long totalUsers;
    private Long studentCount;
    private Long teacherCount;
    private Long totalProjects;
    private Long totalRequests;
    private Long pendingCount;
    private Long acceptedCount;
    private Long totalCapacity;
    private Long filledSlots;
    private List<ChartCountVO> userRoleCounts;
    private List<ChartCountVO> userStatusCounts;
    private List<ChartCountVO> projectStatusCounts;
    private List<ChartCountVO> requestStatusCounts;
    private List<ChartCountVO> categoryCounts;
    private List<ChartCountVO> teacherProjectCounts;
    private List<ChartCountVO> programmeCounts;
    private List<ProjectFillRateVO> fillRateTopProjects;
}