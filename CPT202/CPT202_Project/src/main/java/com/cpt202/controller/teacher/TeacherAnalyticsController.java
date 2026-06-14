package com.cpt202.controller.teacher;

import com.cpt202.context.BaseContext;
import com.cpt202.result.Result;
import com.cpt202.service.AnalyticsService;
import com.cpt202.vo.TeacherAnalyticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师端分析统计接口。
 */
@RestController
@RequestMapping("/api/teacher/analytics")
@RequiredArgsConstructor
@Tag(name = "Teacher Analytics API")
public class TeacherAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = "Get teacher analytics")
    public Result<TeacherAnalyticsVO> getAnalytics() {
        return Result.success(analyticsService.getTeacherAnalytics(BaseContext.getCurrentUserId()));
    }
}