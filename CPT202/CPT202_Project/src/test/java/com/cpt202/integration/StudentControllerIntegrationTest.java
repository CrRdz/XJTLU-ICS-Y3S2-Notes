package com.cpt202.integration;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for student-facing controllers.
 *
 * The goal is to keep the same granularity as the teacher integration test while
 * covering the student-specific responsibilities: profile maintenance, project
 * discovery, request submission and request-history review.
 */
@Cpt202IntegrationTest
class StudentControllerIntegrationTest extends IntegrationTestSupport {

    private TeacherProfile teacherProfile;
    private User studentUser;
    private StudentProfile studentProfile;
    private Project project;
    private String studentAuthorization;
    private String projectTitle;
    private String categoryName;
    private String tagName;

        /** Creates a teacher, a student, and one published project for each test. */
    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        User teacherUser = createUser(
                "teacher" + suffix,
                "teacher" + suffix + "@example.com",
                "Teacher " + suffix,
                User.UserRole.TEACHER
        );
        teacherProfile = createTeacherProfile(teacherUser, "T" + suffix);

        studentUser = createUser(
                "student" + suffix,
                "student" + suffix + "@example.com",
                "Student " + suffix,
                User.UserRole.STUDENT
        );
        studentProfile = createStudentProfile(studentUser, "S" + suffix);

        categoryName = "Category" + suffix;
        tagName = "Tag" + suffix;
        projectTitle = "Student Project " + suffix;

        project = createProject(teacherProfile, createCategory(categoryName), projectTitle, 2);
        createTag(tagName);
        studentAuthorization = authorizationFor(studentUser);
    }

    /**
     * Verifies the student's own profile can be read and updated through the
     * public controller instead of mutating the repository directly.
     */
    @Test
    void studentProfileEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/student/profile/me")
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value(studentUser.getFullName()))
                .andExpect(jsonPath("$.data.email").value(studentUser.getEmail()));

        mockMvc.perform(put("/api/student/profile/me")
                        .header("Authorization", studentAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Updated Student",
                                "email", "updated-student@example.com",
                                "programme", "Computer Science",
                                "enrollmentDate", "2024-10-01",
                                "phone", "18800001111",
                                "interests", "Distributed AI"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        StudentProfile refreshed = studentProfileRepository.findById(studentProfile.getStudentId()).orElseThrow();
        assertThat(refreshed.getProgramme()).isEqualTo("Computer Science");
        assertThat(refreshed.getPhone()).isEqualTo("18800001111");
        assertThat(refreshed.getInterests()).isEqualTo("Distributed AI");
        assertThat(refreshed.getUser().getFullName()).isEqualTo("Updated Student");
        assertThat(refreshed.getUser().getEmail()).isEqualTo("updated-student@example.com");
    }

    /**
     * Covers the student discovery surface end to end: list projects, read a
     * single project, then fetch the tag/category dictionaries used by filters.
     */
    @Test
    void studentProjectDiscoveryEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/student/projects")
                        .header("Authorization", studentAuthorization)
                        .param("keyword", projectTitle)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.projectId == %d)].title", project.getProjectId()))
                        .value(hasItem(projectTitle)));

        mockMvc.perform(get("/api/student/projects/{projectId}", project.getProjectId())
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.title").value(projectTitle));

        mockMvc.perform(get("/api/student/projects/tags")
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagName)));

        mockMvc.perform(get("/api/student/projects/categories")
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].categoryName").value(hasItem(categoryName)));
    }

    /**
     * Exercises the normal request lifecycle from the student's point of view:
     * submit first, confirm the request is visible, then withdraw and verify the
     * persisted status and project state are both updated.
     */
    @Test
    void studentRequestLifecycleEndpointsWork() throws Exception {
        Long requestId = submitRequest("Interested in the project lifecycle.");

        mockMvc.perform(get("/api/student/requests")
                        .header("Authorization", studentAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].requestId").value(requestId))
                .andExpect(jsonPath("$.data.records[0].requestStatus").value("PENDING"));

        mockMvc.perform(put("/api/student/requests/{requestId}/withdraw", requestId)
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        ProjectRequest refreshedRequest = projectRequestRepository.findById(requestId).orElseThrow();
        Project refreshedProject = projectRepository.findById(project.getProjectId()).orElseThrow();
        assertThat(refreshedRequest.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.WITHDRAWN);
        assertThat(refreshedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.AVAILABLE);
    }

    /**
     * History should reflect the order of business events rather than just the
     * final state, so the test creates a request, withdraws it, and checks both
     * timeline entries are exposed through the history endpoint.
     */
    @Test
    void studentRequestHistoryEndpointShowsTimeline() throws Exception {
        Long requestId = submitRequest("Track my request timeline.");

        mockMvc.perform(put("/api/student/requests/{requestId}/withdraw", requestId)
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/student/request-history/{requestId}", requestId)
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].newStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].newStatus").value("WITHDRAWN"));
    }

        /** Submits a student request and returns the persisted request id. */
    private Long submitRequest(String notes) throws Exception {
        mockMvc.perform(post("/api/student/requests")
                        .header("Authorization", studentAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectId", project.getProjectId(),
                                "preferenceRank", 1,
                                "notes", notes))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        return projectRequestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentProfile.getStudentId())
                .stream()
                .findFirst()
                .orElseThrow()
                .getRequestId();
    }
}