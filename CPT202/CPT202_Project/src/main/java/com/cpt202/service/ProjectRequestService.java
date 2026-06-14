package com.cpt202.service;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.dto.StudentProjectRequestQueryDTO;
import com.cpt202.dto.TeacherProjectRequestQueryDTO;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.result.PageResult;
import com.cpt202.vo.ProjectRequestVO;
import com.cpt202.vo.StudentRequestSummaryVO;

import java.util.List;

/**
 * 项目申请服务接口。
 * <p>
 * 负责学生提交申请、撤回申请，以及教师审核申请等业务。
 */
public interface ProjectRequestService {

    /**
     * 学生提交项目申请。
     *
     * @param projectRequestCreateDTO 申请创建参数
     */
    void create(Long studentId, ProjectRequestCreateDTO projectRequestCreateDTO);

    /**
     * 查询学生本人申请列表。
     *
     * @param studentId 学生主键
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listStudentRequests(Long studentId);

    /**
     * 查询学生 dashboard 需要的申请摘要。
     *
     * @param studentId 学生主键
     * @return 聚合后的申请摘要
     */
    StudentRequestSummaryVO getStudentRequestSummary(Long studentId);

    /**
     * 查询学生在某个项目详情页所需的最小申请上下文。
     *
     * @param studentId 学生主键
     * @param projectId 项目主键
     * @return 当前项目相关申请与全局活跃申请的合并结果
     */
    List<ProjectRequestVO> getStudentRequestContext(Long studentId, Long projectId);

    /**
     * 分页查询学生本人申请列表。
     *
     * @param studentId 学生主键
     * @param queryDTO 分页查询参数
     * @return 申请分页结果
     */
    PageResult<ProjectRequestVO> listStudentRequestsPage(Long studentId, StudentProjectRequestQueryDTO queryDTO);

    /**
     * 查询教师待审核或已审核申请列表。
     *
     * @param teacherId 教师主键
     * @param status 申请状态
     * @return 申请展示对象列表
     */
    List<ProjectRequestVO> listTeacherRequests(Long teacherId, ProjectRequest.RequestStatus status);

    /**
     * 查询教师可访问的单条申请详情。
     *
     * @param requestId 申请主键
     * @param teacherId 教师主键
     * @return 申请详情
     */
    ProjectRequestVO getTeacherRequest(Long requestId, Long teacherId);

    /**
     * 分页查询教师申请列表。
     *
     * @param teacherId 教师主键
     * @param queryDTO 分页查询参数
     * @return 申请分页结果
     */
    PageResult<ProjectRequestVO> listTeacherRequestsPage(Long teacherId, TeacherProjectRequestQueryDTO queryDTO);

    /**
     * 学生撤回申请。
     *
     * @param requestId 申请主键
     * @param studentId 学生主键
     */
    void withdraw(Long requestId, Long studentId);

    /**
     * 教师审核申请。
     *
     * @param requestId 申请主键
     * @param projectRequestReviewDTO 审核参数
     */
    void review(Long requestId, Long teacherId, ProjectRequestReviewDTO projectRequestReviewDTO);
}
