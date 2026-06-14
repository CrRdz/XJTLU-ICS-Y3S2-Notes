package com.cpt202.service;

import com.cpt202.vo.AdminAnalyticsVO;
import com.cpt202.vo.TeacherAnalyticsVO;

/**
 * 分析统计服务接口。
 */
public interface AnalyticsService {

    /**
     * 查询教师端分析统计数据。
     *
     * @param teacherId 教师主键
     * @return 教师端分析统计结果
     */
    TeacherAnalyticsVO getTeacherAnalytics(Long teacherId);

    /**
     * 查询管理端分析统计数据。
     *
     * @return 管理端分析统计结果
     */
    AdminAnalyticsVO getAdminAnalytics();
}