package com.cpt202.service;

import com.cpt202.dto.ProjectDTO;
import com.cpt202.dto.ProjectStatusUpdateDTO;
import com.cpt202.dto.StudentProjectQueryDTO;
import com.cpt202.dto.TeacherProjectQueryDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.result.PageResult;
import com.cpt202.vo.ProjectVO;

import java.util.List;

/**
 * 项目服务接口。
 * 负责学生端与教师端项目查询，以及教师端项目维护操作。
 */
public interface ProjectService {

    /**
     * 学生端项目分页查询。
     *
     * @param queryDTO 学生端项目查询参数（关键字 / 分类 / 状态 / 标签 / 分页）
     * @return 分页项目展示对象
     */
    PageResult<ProjectVO> listStudentProjects(StudentProjectQueryDTO queryDTO);

    /**
     * 教师端查询本人项目列表。
     *
     * @param teacherId 教师主键
     * @param status 项目状态
     * @return 项目展示对象列表
     */
    List<ProjectVO> listTeacherProjects(Long teacherId, Project.ProjectStatus status);

    /**
     * 教师端分页查询本人项目列表。
     *
     * @param teacherId 教师主键
     * @param queryDTO 项目分页查询参数
     * @return 分页项目展示对象
     */
    PageResult<ProjectVO> listTeacherProjectsPage(Long teacherId, TeacherProjectQueryDTO queryDTO);

    /**
     * 查询项目详情。
     *
     * @param projectId 项目主键
     * @return 项目展示对象
     */
    ProjectVO getProject(Long projectId);

    /**
     * 查询教师本人名下的项目详情。
     *
     * @param projectId 项目主键
     * @param teacherId 当前教师主键
     * @return 项目展示对象
     */
    ProjectVO getOwnedProject(Long projectId, Long teacherId);

    /**
     * 新增项目。
     *
     * @param projectDTO 项目新增参数
     * @return 新增后的项目展示对象
     */
    ProjectVO create(Long teacherId, ProjectDTO projectDTO);

    /**
     * 修改项目。
     *
     * @param projectId 项目主键
     * @param projectDTO 项目更新参数
     */
    void update(Long projectId, Long teacherId, ProjectDTO projectDTO);

    /**
     * 修改项目状态。
     *
     * @param projectId 项目主键
     * @param projectStatusUpdateDTO 项目状态修改参数
     */
    void changeStatus(Long projectId, Long teacherId, ProjectStatusUpdateDTO projectStatusUpdateDTO);
}
