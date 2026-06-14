package com.cpt202.integration;

import com.cpt202.dto.ProjectRequestCreateDTO;
import com.cpt202.dto.ProjectRequestReviewDTO;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for administrator-facing controllers.
 *
 * The admin role in this codebase is mostly about audit and maintenance, so the
 * test suite focuses on profile management, category/tag CRUD, user-state updates
 * and record visibility after a real teacher/student workflow has taken place.
 */
@Cpt202IntegrationTest
class AdminControllerIntegrationTest extends IntegrationTestSupport {

    private User adminUser;
    private String adminAuthorization;
    private User studentUser;
    private Project project;
    private ProjectRequest acceptedRequest;
    private String boundTagName;

        /** Creates an admin fixture plus one completed teacher-student workflow. */
    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        adminUser = createAdminUser(
                "admin" + suffix,
                "admin" + suffix + "@example.com",
                "Admin " + suffix
        );
        adminAuthorization = authorizationFor(adminUser);

        User teacherUser = createUser(
                "teacher-admin" + suffix,
                "teacher-admin" + suffix + "@example.com",
                "Teacher Admin " + suffix,
                User.UserRole.TEACHER
        );
        TeacherProfile teacherProfile = createTeacherProfile(teacherUser, "TA" + suffix);

        studentUser = createUser(
                "student-admin" + suffix,
                "student-admin" + suffix + "@example.com",
                "Student Admin " + suffix,
                User.UserRole.STUDENT
        );
        StudentProfile studentProfile = createStudentProfile(studentUser, "SA" + suffix);

        project = createProject(teacherProfile, createCategory("AdminFlowCategory" + suffix), "Admin Flow Project " + suffix, 1);
        boundTagName = "AdminFlowTag" + suffix;
        Long boundTagId = createTag(boundTagName).getTagId();
        projectTagService.bindProjectTags(project.getProjectId(), teacherProfile.getTeacherId(), List.of(boundTagId));

        ProjectRequestCreateDTO createDTO = new ProjectRequestCreateDTO();
        createDTO.setProjectId(project.getProjectId());
        createDTO.setPreferenceRank(1);
        createDTO.setNotes("Prepared for admin record assertions.");
        projectRequestService.create(studentProfile.getStudentId(), createDTO);

        acceptedRequest = projectRequestRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentProfile.getStudentId())
                .stream()
                .findFirst()
                .orElseThrow();

        ProjectRequestReviewDTO reviewDTO = new ProjectRequestReviewDTO();
        reviewDTO.setRequestStatus(ProjectRequest.RequestStatus.ACCEPTED);
        reviewDTO.setDecisionComment("Accepted for admin workflow checks.");
        projectRequestService.review(acceptedRequest.getRequestId(), teacherProfile.getTeacherId(), reviewDTO);

        acceptedRequest = projectRequestRepository.findById(acceptedRequest.getRequestId()).orElseThrow();
    }

    /**
     * Profile endpoints are the lightest admin surface, so this test checks the
     * token-authenticated read/update path before moving on to broader admin APIs.
     */
    @Test
    void adminProfileEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/profile/me")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value(adminUser.getFullName()))
                .andExpect(jsonPath("$.data.email").value(adminUser.getEmail()));

        mockMvc.perform(put("/api/admin/profile/me")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Updated Admin",
                                "email", "updated-admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        User refreshedAdmin = userRepository.findById(adminUser.getUserId()).orElseThrow();
        assertThat(refreshedAdmin.getFullName()).isEqualTo("Updated Admin");
        assertThat(refreshedAdmin.getEmail()).isEqualTo("updated-admin@example.com");
    }

    /**
     * Category CRUD is isolated with unique names so the assertions stay stable
     * even when seed data or other tests populate the same in-memory database.
     */
    @Test
    void adminCategoryEndpointsWork() throws Exception {
        String suffix = uniqueSuffix();
        String categoryName = "ManagedCategory" + suffix;

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName,
                                "description", "Created by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Long categoryId = categoryRepository.findAll().stream()
                .filter(category -> categoryName.equals(category.getCategoryName()))
                .findFirst()
                .orElseThrow()
                .getCategoryId();

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].categoryName").value(hasItem(categoryName)));

        mockMvc.perform(get("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryName").value(categoryName));

        mockMvc.perform(put("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName + "Updated",
                                "description", "Updated by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(categoryRepository.findById(categoryId).orElseThrow().getCategoryName())
                .isEqualTo(categoryName + "Updated");

        mockMvc.perform(delete("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(categoryRepository.findById(categoryId)).isEmpty();
    }

    /**
     * Tag CRUD mirrors category CRUD but uses a different resource, so keeping it
     * in its own test makes failures point directly to the broken admin surface.
     */
    @Test
    void adminTagEndpointsWork() throws Exception {
        String suffix = uniqueSuffix();
        String tagName = "ManagedTag" + suffix;

        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName,
                                "description", "Created by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        Long tagId = tagRepository.findAll().stream()
                .filter(tag -> tagName.equals(tag.getTagName()))
                .findFirst()
                .orElseThrow()
                .getTagId();

        mockMvc.perform(get("/api/admin/tags")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagName)));

        mockMvc.perform(get("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagName").value(tagName));

        mockMvc.perform(put("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName + "Updated",
                                "description", "Updated by admin integration test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(tagRepository.findById(tagId).orElseThrow().getTagName()).isEqualTo(tagName + "Updated");

        mockMvc.perform(delete("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(tagRepository.findById(tagId)).isEmpty();
    }

    /**
     * User management is one of the few write operations owned solely by admin,
     * so the test validates both filtered listing and status mutation.
     */
    @Test
    void adminUserEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminAuthorization)
                        .param("role", "STUDENT")
                        .param("accountStatus", "ACTIVE")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.userId == %d)].accountStatus", studentUser.getUserId()))
                        .value(hasItem("ACTIVE")));

        mockMvc.perform(put("/api/admin/users/{userId}/status", studentUser.getUserId())
                        .header("Authorization", adminAuthorization)
                        .param("accountStatus", "DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        assertThat(userRepository.findById(studentUser.getUserId()).orElseThrow().getAccountStatus())
                .isEqualTo("DISABLED");
    }

    /**
     * Record endpoints are the admin-side view of the entire business workflow.
     * This test verifies the admin can see the closed project, the accepted
     * request, the request history and the project's bound tags.
     */
    @Test
    void adminRecordAndProjectTagEndpointsWork() throws Exception {
        mockMvc.perform(get("/api/admin/records/projects")
                        .header("Authorization", adminAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.projectId == %d)].projectStatus", project.getProjectId()))
                        .value(hasItem("CLOSED")));

        mockMvc.perform(get("/api/admin/records/requests")
                        .header("Authorization", adminAuthorization)
                        .param("status", "ACCEPTED")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].requestStatus", acceptedRequest.getRequestId()))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/records/request-history")
                        .header("Authorization", adminAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].newStatus", acceptedRequest.getRequestId()))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/projects/{projectId}/tags", project.getProjectId())
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(boundTagName)));
    }

    /**
     * Admin analytics should return backend-aggregated system statistics instead
     * of forcing the UI to fetch every list and count the rows client-side.
     */
    @Test
    void adminAnalyticsEndpointReturnsAggregatedStats() throws Exception {
        User duplicateTeacherUserA = createUser(
                "duplicate-admin-teacher-a-" + uniqueSuffix(),
                "duplicate-admin-teacher-a-" + uniqueSuffix() + "@example.com",
                "Duplicate Teacher",
                User.UserRole.TEACHER
        );
        TeacherProfile duplicateTeacherProfileA = createTeacherProfile(duplicateTeacherUserA, "DUP-A-" + uniqueSuffix());
        createProject(
                duplicateTeacherProfileA,
                createCategory("DuplicateTeacherCategoryA" + uniqueSuffix()),
                "Duplicate Teacher Project A",
                2
        );

        User duplicateTeacherUserB = createUser(
                "duplicate-admin-teacher-b-" + uniqueSuffix(),
                "duplicate-admin-teacher-b-" + uniqueSuffix() + "@example.com",
                "Duplicate Teacher",
                User.UserRole.TEACHER
        );
        TeacherProfile duplicateTeacherProfileB = createTeacherProfile(duplicateTeacherUserB, "DUP-B-" + uniqueSuffix());
        createProject(
                duplicateTeacherProfileB,
                createCategory("DuplicateTeacherCategoryB" + uniqueSuffix()),
                "Duplicate Teacher Project B",
                2
        );

        long expectedTotalUsers = userRepository.count();
        long expectedTotalProjects = projectRepository.count();
        long expectedTotalRequests = projectRequestRepository.count();
        long expectedPendingCount = projectRequestRepository.findAll().stream()
                .filter(projectRequest -> projectRequest.getRequestStatus() == ProjectRequest.RequestStatus.PENDING)
                .count();
        long expectedAcceptedCount = projectRequestRepository.findAll().stream()
                .filter(projectRequest -> projectRequest.getRequestStatus() == ProjectRequest.RequestStatus.ACCEPTED)
                .count();
        long expectedTotalCapacity = projectRepository.findAll().stream()
                .map(Project::getMaxStudents)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long expectedFilledSlots = projectRepository.findAll().stream()
                .map(Project::getCurrentAgreedCount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        Project topFillRateProject = projectRepository.findAll().stream()
                                .sorted((leftProject, rightProject) -> {
                                        int fillRateCompare = Double.compare(fillRate(rightProject), fillRate(leftProject));
                                        if (fillRateCompare != 0) {
                                                return fillRateCompare;
                                        }
                                        String leftTitle = leftProject.getTitle() == null ? "" : leftProject.getTitle();
                                        String rightTitle = rightProject.getTitle() == null ? "" : rightProject.getTitle();
                                        return leftTitle.compareTo(rightTitle);
                                })
                                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/admin/analytics")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.totalUsers").value(expectedTotalUsers))
                .andExpect(jsonPath("$.data.studentCount").value(userRepository.countByRole(User.UserRole.STUDENT)))
                .andExpect(jsonPath("$.data.teacherCount").value(userRepository.countByRole(User.UserRole.TEACHER)))
                .andExpect(jsonPath("$.data.totalProjects").value(expectedTotalProjects))
                .andExpect(jsonPath("$.data.totalRequests").value(expectedTotalRequests))
                .andExpect(jsonPath("$.data.pendingCount").value(expectedPendingCount))
                .andExpect(jsonPath("$.data.acceptedCount").value(expectedAcceptedCount))
                .andExpect(jsonPath("$.data.totalCapacity").value(expectedTotalCapacity))
                .andExpect(jsonPath("$.data.filledSlots").value(expectedFilledSlots))
                .andExpect(jsonPath("$.data.userRoleCounts[*].label").value(hasItem("ADMIN")))
                .andExpect(jsonPath("$.data.userRoleCounts[*].label").value(hasItem("TEACHER")))
                .andExpect(jsonPath("$.data.userRoleCounts[*].label").value(hasItem("STUDENT")))
                .andExpect(jsonPath("$.data.requestStatusCounts[*].label").value(hasItem("ACCEPTED")))
                .andExpect(jsonPath("$.data.projectStatusCounts[*].label").value(hasItem("CLOSED")))
                .andExpect(jsonPath("$.data.categoryCounts[*].label")
                        .value(hasItem(project.getCategory().getCategoryName())))
                .andExpect(jsonPath("$.data.teacherProjectCounts[*].label")
                        .value(hasItem("Duplicate Teacher (" + duplicateTeacherProfileA.getStaffNo() + ")")))
                .andExpect(jsonPath("$.data.teacherProjectCounts[*].label")
                        .value(hasItem("Duplicate Teacher (" + duplicateTeacherProfileB.getStaffNo() + ")")))
                .andExpect(jsonPath("$.data.programmeCounts[*].label").value(hasItem("Software Engineering")))
                .andExpect(jsonPath("$.data.fillRateTopProjects[0].name").value(topFillRateProject.getTitle()));
    }

        private double fillRate(Project project) {
                int maxStudents = project.getMaxStudents() == null ? 0 : project.getMaxStudents();
                int currentAgreedCount = project.getCurrentAgreedCount() == null ? 0 : project.getCurrentAgreedCount();
                return maxStudents <= 0 ? 0D : (double) currentAgreedCount / maxStudents;
        }
}