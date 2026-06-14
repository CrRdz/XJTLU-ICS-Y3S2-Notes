package com.cpt202.integration;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.Tag;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for teacher-facing controllers.
 *
 * The suite reuses the shared fixtures so every check stays focused on the
 * teacher role itself: profile maintenance, project management, tag binding and
 * request review.
 */
@Cpt202IntegrationTest
class TeacherControllerIntegrationTest extends IntegrationTestSupport {

    private User teacherUser;
    private TeacherProfile teacherProfile;
    private StudentProfile studentProfile;
        private Long categoryId;
    private Tag tagA;
    private Tag tagB;
    private Project project;
    private ProjectRequest request;
    private String teacherAuthorization;

        /** Creates a teacher-owned workflow fixture before each test. */
    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        teacherUser = createUser(
                "teacher" + suffix,
                "teacher" + suffix + "@example.com",
                "Teacher " + suffix,
                User.UserRole.TEACHER
        );
        teacherProfile = createTeacherProfile(teacherUser, "T" + suffix);

        User studentUser = createUser(
                "student-for-teacher" + suffix,
                "student-for-teacher" + suffix + "@example.com",
                "Student For Teacher " + suffix,
                User.UserRole.STUDENT
        );
        studentProfile = createStudentProfile(studentUser, "S" + suffix);

        categoryId = createCategory("TeacherCategory" + suffix).getCategoryId();
        tagA = createTag("TeacherTagA" + suffix);
        tagB = createTag("TeacherTagB" + suffix);
        project = createProject(teacherProfile, categoryRepository.findById(categoryId).orElseThrow(), "Initial Project " + suffix, 2);
        request = createPendingRequest(project, studentProfile, 1, "Interested in teacher workflow.");
        teacherAuthorization = authorizationFor(teacherUser);
    }

    /**
     * Verifies the teacher can read and update the role-owned profile surface.
     */
    @Test
    void teacherProfileEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.teacherId").value(teacherProfile.getTeacherId()))
                .andExpect(jsonPath("$.data.fullName").value(teacherUser.getFullName()));

        mockMvc.perform(put("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Teacher Updated",
                                "email", "teacher-updated@example.com",
                                "department", "Data Science",
                                "title", "Associate Professor",
                                "researchArea", "LLM Systems",
                                "office", "Room 202"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        TeacherProfile refreshed = teacherProfileRepository.findById(teacherProfile.getTeacherId()).orElseThrow();
        assertThat(refreshed.getDepartment()).isEqualTo("Data Science");
        assertThat(refreshed.getUser().getFullName()).isEqualTo("Teacher Updated");
        assertThat(refreshed.getUser().getEmail()).isEqualTo("teacher-updated@example.com");
    }

    /**
     * Exercises the teacher's project-management workflow: create, list, read,
     * update and finally close a project through HTTP.
     */
    @Test
    void teacherProjectEndpointsWork() throws Exception {
        String createdTitle = "New Project " + uniqueSuffix();

        mockMvc.perform(post("/api/teacher/projects")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryId", categoryId,
                                "title", createdTitle,
                                "description", "New Description",
                                "requiredSkills", "Spring Boot",
                                "topicArea", "Web",
                                "maxStudents", 3,
                                "closeDate", LocalDateTime.now().plusDays(7).withSecond(0).withNano(0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.title").value(createdTitle));

        Project createdProject = projectRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherProfile.getTeacherId())
                .stream()
                .filter(savedProject -> createdTitle.equals(savedProject.getTitle()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/teacher/projects")
                        .header("Authorization", teacherAuthorization)
                        .param("status", "AVAILABLE")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.projectId == %d)].title", createdProject.getProjectId()))
                        .value(hasItem(createdTitle)));

        mockMvc.perform(get("/api/teacher/projects/{projectId}", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(createdTitle));

        mockMvc.perform(put("/api/teacher/projects/{projectId}", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryId", categoryId,
                                "title", "Updated Project",
                                "description", "Updated Description",
                                "requiredSkills", "Spring Data",
                                "topicArea", "Persistence",
                                "maxStudents", 4,
                                "closeDate", LocalDateTime.now().plusDays(10).withSecond(0).withNano(0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(put("/api/teacher/projects/{projectId}/status", createdProject.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectStatus", "CLOSED",
                                "remark", "Manual close"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Project refreshed = projectRepository.findById(createdProject.getProjectId()).orElseThrow();
        assertThat(refreshed.getTitle()).isEqualTo("Updated Project");
        assertThat(refreshed.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
    }

    /**
     * Tag binding should be visible immediately through the teacher-owned tag
     * endpoints because the UI relies on that round-trip behavior.
     */
    @Test
    void teacherProjectTagEndpointsWork() throws Exception {
        mockMvc.perform(put("/api/teacher/project-tags/{projectId}", project.getProjectId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagIds", List.of(tagA.getTagId(), tagB.getTagId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/teacher/project-tags/{projectId}", project.getProjectId())
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagA.getTagName())))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagB.getTagName())));
    }

    /**
     * Reviews should be scoped to the teacher's own pending queue and the final
     * decision must persist back to the request aggregate.
     */
    @Test
    void teacherRequestEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/teacher/requests")
                        .header("Authorization", teacherAuthorization)
                        .param("status", "PENDING")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].requestStatus", request.getRequestId()))
                        .value(hasItem("PENDING")));

        mockMvc.perform(put("/api/teacher/requests/{requestId}/review", request.getRequestId())
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "requestStatus", "REJECTED",
                                "decisionComment", "Not a match"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ProjectRequest refreshed = projectRequestRepository.findById(request.getRequestId()).orElseThrow();
        assertThat(refreshed.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.REJECTED);
        assertThat(refreshed.getReviewedBy().getTeacherId()).isEqualTo(teacherProfile.getTeacherId());
    }

    /**
     * Analytics should be aggregated on the backend and scoped to the current
     * teacher rather than requiring the client to fetch and count raw rows.
     */
    @Test
    void teacherAnalyticsEndpointReturnsAggregatedStats() throws Exception {
        mockMvc.perform(get("/api/teacher/analytics")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.totalProjects").value(1))
                .andExpect(jsonPath("$.data.totalRequests").value(1))
                .andExpect(jsonPath("$.data.pendingCount").value(1))
                .andExpect(jsonPath("$.data.acceptedCount").value(0))
                .andExpect(jsonPath("$.data.rejectedCount").value(0))
                .andExpect(jsonPath("$.data.withdrawnCount").value(0))
                .andExpect(jsonPath("$.data.totalCapacity").value(2))
                .andExpect(jsonPath("$.data.filledSlots").value(0))
                .andExpect(jsonPath("$.data.requestStatusCounts[?(@.label == 'PENDING')].value")
                        .value(hasItem(1)))
                .andExpect(jsonPath("$.data.projectStatusCounts[?(@.label == 'AVAILABLE')].value")
                        .value(hasItem(1)))
                .andExpect(jsonPath("$.data.requestsPerProject[0].label").value(project.getTitle()))
                .andExpect(jsonPath("$.data.requestsPerProject[0].value").value(1))
                .andExpect(jsonPath("$.data.programmeCounts[?(@.label == 'Software Engineering')].value")
                        .value(hasItem(1)))
                .andExpect(jsonPath("$.data.preferenceRankCounts[?(@.label == '1')].value")
                        .value(hasItem(1)))
                .andExpect(jsonPath("$.data.fillRateTopProjects[0].name").value(project.getTitle()))
                .andExpect(jsonPath("$.data.fillRateTopProjects[0].current").value(0))
                .andExpect(jsonPath("$.data.fillRateTopProjects[0].max").value(2))
                .andExpect(jsonPath("$.data.fillRateTopProjects[0].rate").value(0));
    }
}
