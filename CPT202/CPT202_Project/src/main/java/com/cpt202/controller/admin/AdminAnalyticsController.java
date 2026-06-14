package com.cpt202.controller.admin;

import com.cpt202.result.Result;
import com.cpt202.service.AnalyticsService;
import com.cpt202.vo.AdminAnalyticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin management analysis and statistics interface.
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics API")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = "Get admin analytics")
    public Result<AdminAnalyticsVO> getAnalytics() {
        return Result.success(analyticsService.getAdminAnalytics());
    }
}