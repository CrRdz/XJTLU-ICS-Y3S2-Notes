package com.cpt202.acceptance;

import com.cpt202.integration.IntegrationTestSupport;
import com.cpt202.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Acceptance stories for self-service profile management. */
@Cpt202AcceptanceTest
class SelfServiceProfileAcceptanceTest extends IntegrationTestSupport {

    private User studentUser;
    private User teacherUser;
    private User adminUser;
    private String studentAuthorization;
    private String teacherAuthorization;
    private String adminAuthorization;

    /** Creates one student, one teacher, and one admin for profile stories. */
    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        studentUser = createUser(
                "acceptance-profile-student-" + suffix,
                "acceptance-profile-student-" + suffix + "@example.com",
                "Acceptance Profile Student " + suffix,
                User.UserRole.STUDENT
        );
        createStudentProfile(studentUser, "APS" + suffix);

        teacherUser = createUser(
                "acceptance-profile-teacher-" + suffix,
                "acceptance-profile-teacher-" + suffix + "@example.com",
                "Acceptance Profile Teacher " + suffix,
                User.UserRole.TEACHER
        );
        createTeacherProfile(teacherUser, "APT" + suffix);

        adminUser = createAdminUser(
                "acceptance-profile-admin-" + suffix,
                "acceptance-profile-admin-" + suffix + "@example.com",
                "Acceptance Profile Admin " + suffix
        );

        studentAuthorization = authorizationFor(studentUser);
        teacherAuthorization = authorizationFor(teacherUser);
        adminAuthorization = authorizationFor(adminUser);
    }

    /** Student can review and update their own profile. */
    @Test
    void studentCanReviewAndUpdateOwnProfile() throws Exception {
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
                                "fullName", "Updated Acceptance Student",
                                "email", "updated-acceptance-student@example.com",
                                "programme", "Computer Science",
                                "enrollmentDate", "2024-10-01",
                                "phone", "18800001111",
                                "interests", "Release Testing"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/student/profile/me")
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value("Updated Acceptance Student"))
                .andExpect(jsonPath("$.data.email").value("updated-acceptance-student@example.com"))
                .andExpect(jsonPath("$.data.programme").value("Computer Science"))
                .andExpect(jsonPath("$.data.interests").value("Release Testing"));
    }

    /** Teacher can review and update their own profile. */
    @Test
    void teacherCanReviewAndUpdateOwnProfile() throws Exception {
        mockMvc.perform(get("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value(teacherUser.getFullName()))
                .andExpect(jsonPath("$.data.email").value(teacherUser.getEmail()));

        mockMvc.perform(put("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Updated Acceptance Teacher",
                                "email", "updated-acceptance-teacher@example.com",
                                "department", "Data Science",
                                "title", "Associate Professor",
                                "researchArea", "AI Systems",
                                "office", "Room 302"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/teacher/profile/me")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value("Updated Acceptance Teacher"))
                .andExpect(jsonPath("$.data.email").value("updated-acceptance-teacher@example.com"))
                .andExpect(jsonPath("$.data.department").value("Data Science"))
                .andExpect(jsonPath("$.data.office").value("Room 302"));
    }

    /** Administrator can review and update their own profile. */
    @Test
    void administratorCanReviewAndUpdateOwnProfile() throws Exception {
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
                                "fullName", "Updated Acceptance Admin",
                                "email", "updated-acceptance-admin@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/admin/profile/me")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.fullName").value("Updated Acceptance Admin"))
                .andExpect(jsonPath("$.data.email").value("updated-acceptance-admin@example.com"));
    }
}