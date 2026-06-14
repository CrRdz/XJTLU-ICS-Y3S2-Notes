package com.cpt202.service;

import com.cpt202.vo.ProjectTagVO;

import java.util.List;

/**
 * 项目标签服务接口。
 * <p>
 * 用于查询项目已绑定标签，以及教师为项目重新绑定标签。
 */
public interface ProjectTagService {

    /**
     * 查询项目标签列表。
     *
     * @param projectId 项目主键
     * @return 项目标签展示对象列表
     */
    List<ProjectTagVO> listProjectTags(Long projectId);

    /**
     * 为指定项目绑定标签。
     *
     * @param projectId 项目主键
     * @param teacherId 教师主键
     * @param tagIds 标签主键列表
     */
    void bindProjectTags(Long projectId, Long teacherId, List<Long> tagIds);
}
