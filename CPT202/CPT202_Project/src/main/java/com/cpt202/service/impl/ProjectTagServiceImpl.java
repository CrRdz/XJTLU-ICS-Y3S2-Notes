package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectTag;
import com.cpt202.model.entity.ProjectTagId;
import com.cpt202.model.entity.Tag;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectTagRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.ProjectTagService;
import com.cpt202.vo.ProjectTagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目标签服务实现类。
 * 负责查询项目已绑定标签，以及处理项目标签重绑定。
 */
@Service
@RequiredArgsConstructor
public class ProjectTagServiceImpl implements ProjectTagService {

    private final ProjectTagRepository projectTagRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;

    /**
     * 查询项目标签列表。
     */
    @Override
    public List<ProjectTagVO> listProjectTags(Long projectId) {
        List<ProjectTag> projectTags = projectTagRepository.findByProject_ProjectIdOrderByTag_TagIdAsc(projectId);
        List<ProjectTagVO> tagVos = new ArrayList<>(projectTags.size());
        for (ProjectTag projectTag : projectTags) {
            tagVos.add(new ProjectTagVO(
                    projectTag.getProject().getProjectId(),
                    projectTag.getTag().getTagId(),
                    projectTag.getTag().getTagName()));
        }
        return tagVos;
    }

    /**
     * 绑定项目标签。
     */
    @Override
    @Transactional
    public void bindProjectTags(Long projectId, Long teacherId, List<Long> tagIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.PROJECT_NOT_FOUND));
        if (project.getTeacher() == null || !teacherId.equals(project.getTeacher().getTeacherId())) {
            throw new BusinessException(MessageConstants.CANNOT_UPDATE_OTHER_TEACHER_TAGS);
        }

        List<Tag> tags = tagRepository.findAllById(tagIds);
        if (tags.size() != tagIds.size()) {
            throw new NotFoundException(MessageConstants.PARTIAL_TAGS_NOT_FOUND);
        }

        projectTagRepository.deleteByProject_ProjectId(projectId);
        List<ProjectTag> projectTags = new ArrayList<>(tags.size());
        for (Tag tag : tags) {
            ProjectTag projectTag = new ProjectTag();
            projectTag.setId(new ProjectTagId(projectId, tag.getTagId()));
            projectTag.setProject(project);
            projectTag.setTag(tag);
            projectTags.add(projectTag);
        }
        projectTagRepository.saveAll(projectTags);
    }
}
