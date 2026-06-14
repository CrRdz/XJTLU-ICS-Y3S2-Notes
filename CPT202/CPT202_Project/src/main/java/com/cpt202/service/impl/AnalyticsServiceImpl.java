package com.cpt202.service.impl;

import com.cpt202.model.entity.User;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.AnalyticsService;
import com.cpt202.vo.AdminAnalyticsVO;
import com.cpt202.vo.ChartCountVO;
import com.cpt202.vo.ProjectFillRateVO;
import com.cpt202.vo.TeacherAnalyticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分析统计服务实现。
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int TOP_N = 10;

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRequestRepository projectRequestRepository;

    @Override
    public TeacherAnalyticsVO getTeacherAnalytics(Long teacherId) {
        List<ChartCountVO> requestStatusCounts = toChartCounts(projectRequestRepository.countTeacherRequestsByStatus(teacherId));
        List<ChartCountVO> projectStatusCounts = toChartCounts(projectRepository.countTeacherProjectsByStatus(teacherId));

        return TeacherAnalyticsVO.builder()
                .totalProjects(projectRepository.countByTeacher_TeacherId(teacherId))
                .totalRequests(projectRequestRepository.countByProject_Teacher_TeacherId(teacherId))
                .pendingCount(findCount(requestStatusCounts, "PENDING"))
                .acceptedCount(findCount(requestStatusCounts, "ACCEPTED"))
                .rejectedCount(findCount(requestStatusCounts, "REJECTED"))
                .withdrawnCount(findCount(requestStatusCounts, "WITHDRAWN"))
                .totalCapacity(defaultLong(projectRepository.sumTeacherCapacity(teacherId)))
                .filledSlots(defaultLong(projectRepository.sumTeacherFilledSlots(teacherId)))
                .requestStatusCounts(requestStatusCounts)
                .projectStatusCounts(projectStatusCounts)
                .requestsPerProject(toChartCounts(
                        projectRequestRepository.countTeacherRequestsPerProject(teacherId, PageRequest.of(0, TOP_N))))
                .programmeCounts(toChartCounts(projectRequestRepository.countTeacherRequestsByProgramme(teacherId)))
                .preferenceRankCounts(toPreferenceRankCounts(
                        projectRequestRepository.countTeacherRequestsByPreferenceRank(teacherId)))
                .fillRateTopProjects(toFillRates(
                        projectRepository.findTeacherProjectFillRates(teacherId, PageRequest.of(0, TOP_N))))
                .build();
    }

    @Override
    public AdminAnalyticsVO getAdminAnalytics() {
        List<ChartCountVO> requestStatusCounts = toChartCounts(projectRequestRepository.countRequestsByStatus());

        return AdminAnalyticsVO.builder()
                .totalUsers(userRepository.count())
                .studentCount(userRepository.countByRole(User.UserRole.STUDENT))
                .teacherCount(userRepository.countByRole(User.UserRole.TEACHER))
                .totalProjects(projectRepository.count())
                .totalRequests(projectRequestRepository.count())
                .pendingCount(findCount(requestStatusCounts, "PENDING"))
                .acceptedCount(findCount(requestStatusCounts, "ACCEPTED"))
                .totalCapacity(defaultLong(projectRepository.sumTotalCapacity()))
                .filledSlots(defaultLong(projectRepository.sumFilledSlots()))
                .userRoleCounts(toChartCounts(userRepository.countUsersByRole()))
                .userStatusCounts(toChartCounts(userRepository.countUsersByAccountStatus()))
                .projectStatusCounts(toChartCounts(projectRepository.countProjectsByStatus()))
                .requestStatusCounts(requestStatusCounts)
                .categoryCounts(toChartCounts(projectRepository.countProjectsByCategory()))
                .teacherProjectCounts(toChartCounts(projectRepository.countProjectsByTeacher(PageRequest.of(0, TOP_N))))
                .programmeCounts(toChartCounts(projectRequestRepository.countRequestsByProgramme()))
                .fillRateTopProjects(toFillRates(projectRepository.findProjectFillRates(PageRequest.of(0, TOP_N))))
                .build();
    }

    private List<ChartCountVO> toChartCounts(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> new ChartCountVO(toLabel(row[0]), toLong(row[1])))
                .collect(Collectors.toList());
    }

    private List<ChartCountVO> toPreferenceRankCounts(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> new ChartCountVO(String.valueOf(row[0]), toLong(row[1])))
                .collect(Collectors.toList());
    }

    private List<ProjectFillRateVO> toFillRates(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        return rows.stream()
                .map(row -> {
                    int current = toInteger(row[1]);
                    int max = toInteger(row[2]);
                    int rate = max <= 0 ? 0 : (int) Math.round(current * 100.0 / max);
                    return new ProjectFillRateVO(String.valueOf(row[0]), current, max, rate);
                })
                .collect(Collectors.toList());
    }

    private Long findCount(List<ChartCountVO> rows, String label) {
        return rows.stream()
                .filter(row -> Objects.equals(row.getLabel(), label))
                .map(ChartCountVO::getValue)
                .findFirst()
                .orElse(0L);
    }

    private String toLabel(Object value) {
        if (value == null) {
            return "UNKNOWN";
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}