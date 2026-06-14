package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.BusinessException;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectTag;
import com.cpt202.model.entity.ProjectTagId;
import com.cpt202.model.entity.Tag;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectTagRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.service.impl.ProjectTagServiceImpl;
import com.cpt202.vo.ProjectTagVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for project-tag listing and binding rules. */
@ExtendWith(MockitoExtension.class)
class ProjectTagServiceImplTest {

    @Mock
    private ProjectTagRepository projectTagRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ProjectTagServiceImpl projectTagService;

    /** Maps project-tag relations to ordered view objects. */
    @Test
    void listProjectTagsShouldMapRelationsToVo() {
        Project project = project(10L, 2L);
        Tag java = tag(1L, "Java");
        Tag spring = tag(3L, "Spring");

        when(projectTagRepository.findByProject_ProjectIdOrderByTag_TagIdAsc(10L))
                .thenReturn(List.of(projectTag(project, java), projectTag(project, spring)));

        List<ProjectTagVO> result = projectTagService.listProjectTags(10L);

        assertThat(result).extracting(ProjectTagVO::getProjectId).containsOnly(10L);
        assertThat(result).extracting(ProjectTagVO::getTagId).containsExactly(1L, 3L);
        assertThat(result).extracting(ProjectTagVO::getTagName).containsExactly("Java", "Spring");
    }

    /** Rejects tag binding when the teacher does not own the project. */
    @Test
    void bindProjectTagsShouldRejectWhenTeacherDoesNotOwnProject() {
        Project project = project(11L, 100L);

        when(projectRepository.findById(11L)).thenReturn(Optional.of(project));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> projectTagService.bindProjectTags(11L, 200L, List.of(1L, 2L)));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.CANNOT_UPDATE_OTHER_TEACHER_TAGS);
        verify(projectTagRepository, never()).deleteByProject_ProjectId(11L);
    }

    /** Rejects tag binding when not all requested tag ids resolve. */
    @Test
    void bindProjectTagsShouldRejectWhenSomeTagsAreMissing() {
        Project project = project(12L, 300L);
        Tag java = tag(1L, "Java");

        when(projectRepository.findById(12L)).thenReturn(Optional.of(project));
        when(tagRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(java));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> projectTagService.bindProjectTags(12L, 300L, List.of(1L, 2L)));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.PARTIAL_TAGS_NOT_FOUND);
        verify(projectTagRepository, never()).deleteByProject_ProjectId(12L);
    }

    /** Replaces existing bindings with the resolved set of project tags. */
    @Test
    void bindProjectTagsShouldReplaceExistingBindingsWithResolvedTags() {
        Project project = project(13L, 400L);
        Tag java = tag(7L, "Java");
        Tag spring = tag(8L, "Spring");
        AtomicReference<List<ProjectTag>> savedProjectTags = new AtomicReference<>();

        when(projectRepository.findById(13L)).thenReturn(Optional.of(project));
        when(tagRepository.findAllById(List.of(7L, 8L))).thenReturn(List.of(java, spring));
        when(projectTagRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProjectTag> projectTags = invocation.getArgument(0);
            savedProjectTags.set(projectTags);
            return projectTags;
        });

        projectTagService.bindProjectTags(13L, 400L, List.of(7L, 8L));

        verify(projectTagRepository).deleteByProject_ProjectId(13L);
        verify(projectTagRepository).saveAll(anyList());

        List<ProjectTag> saved = savedProjectTags.get();
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(item -> item.getId().getProjectId()).containsOnly(13L);
        assertThat(saved).extracting(item -> item.getId().getTagId()).containsExactly(7L, 8L);
        assertThat(saved).extracting(ProjectTag::getProject).containsOnly(project);
        assertThat(saved).extracting(item -> item.getTag().getTagName()).containsExactly("Java", "Spring");
    }

    private Project project(Long projectId, Long teacherId) {
        Project project = new Project();
        ReflectionTestUtils.setField(project, "projectId", projectId);
        project.setTeacher(teacherProfile(teacherId));
        return project;
    }

    private TeacherProfile teacherProfile(Long teacherId) {
        TeacherProfile teacherProfile = new TeacherProfile();
        ReflectionTestUtils.setField(teacherProfile, "teacherId", teacherId);
        User user = new User();
        user.setUserId(teacherId);
        user.setRole(User.UserRole.TEACHER);
        teacherProfile.setUser(user);
        return teacherProfile;
    }

    private Tag tag(Long tagId, String tagName) {
        Tag tag = new Tag();
        ReflectionTestUtils.setField(tag, "tagId", tagId);
        tag.setTagName(tagName);
        return tag;
    }

    private ProjectTag projectTag(Project project, Tag tag) {
        ProjectTag projectTag = new ProjectTag();
        projectTag.setId(new ProjectTagId(project.getProjectId(), tag.getTagId()));
        projectTag.setProject(project);
        projectTag.setTag(tag);
        return projectTag;
    }
}