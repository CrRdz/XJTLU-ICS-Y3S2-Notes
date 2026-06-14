package com.cpt202.support;

import com.cpt202.integration.IntegrationTestSupport;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.Tag;
import com.cpt202.model.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Shared HTTP steps for project-selection workflows. */
public abstract class ProjectSelectionWorkflowSupport extends IntegrationTestSupport {

    protected User adminUser;
    protected User teacherUser;
    protected User studentUser;
    protected StudentProfile studentProfile;
    protected String adminAuthorization;
    protected String teacherAuthorization;
    protected String studentAuthorization;
    protected Long categoryId;
    protected Tag tagA;
    protected Tag tagB;
    protected String projectTitle;

        /** Creates the three actors and shared reference data. */
    protected void initializeProjectSelectionActors(String prefix) {
        String suffix = uniqueSuffix();

        adminUser = createAdminUser(
                prefix.toLowerCase() + "-admin" + suffix,
                prefix.toLowerCase() + "-admin" + suffix + "@example.com",
                prefix + " Admin " + suffix
        );
        teacherUser = createUser(
                prefix.toLowerCase() + "-teacher" + suffix,
                prefix.toLowerCase() + "-teacher" + suffix + "@example.com",
                prefix + " Teacher " + suffix,
                User.UserRole.TEACHER
        );
        studentUser = createUser(
                prefix.toLowerCase() + "-student" + suffix,
                prefix.toLowerCase() + "-student" + suffix + "@example.com",
                prefix + " Student " + suffix,
                User.UserRole.STUDENT
        );

        createTeacherProfile(teacherUser, prefix.substring(0, 1).toUpperCase() + "T" + suffix);
        studentProfile = createStudentProfile(studentUser, prefix.substring(0, 1).toUpperCase() + "S" + suffix);

        categoryId = createCategory(prefix + "Category" + suffix).getCategoryId();
        tagA = createTag(prefix + "TagA" + suffix);
        tagB = createTag(prefix + "TagB" + suffix);
        projectTitle = prefix + " Project " + suffix;

        adminAuthorization = authorizationFor(adminUser);
        teacherAuthorization = authorizationFor(teacherUser);
        studentAuthorization = authorizationFor(studentUser);
    }

        /** Teacher creates a project and returns its id. */
    protected Long teacherPublishesProject(String description, int maxStudents) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/teacher/projects")
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryId", categoryId,
                                "title", projectTitle,
                                "description", description,
                                "requiredSkills", "Java, Spring Boot",
                                "topicArea", "Release Readiness",
                                "maxStudents", maxStudents,
                                "closeDate", LocalDateTime.now().plusDays(7).withSecond(0).withNano(0)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.title").value(projectTitle))
                .andReturn();

        return readDataLong(result, "projectId");
    }

        /** Teacher binds the shared tags to the project. */
    protected void teacherBindsProjectTags(Long projectId) throws Exception {
        mockMvc.perform(put("/api/teacher/project-tags/{projectId}", projectId)
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagIds", List.of(tagA.getTagId(), tagB.getTagId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/teacher/project-tags/{projectId}", projectId)
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagA.getTagName())))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagB.getTagName())));
    }

        /** Student lists the project and opens its detail page. */
    protected void studentCanDiscoverProject(Long projectId) throws Exception {
        mockMvc.perform(get("/api/student/projects")
                        .header("Authorization", studentAuthorization)
                        .param("keyword", projectTitle)
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.projectId == %d)].title", projectId))
                        .value(hasItem(projectTitle)));

        mockMvc.perform(get("/api/student/projects/{projectId}", projectId)
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.title").value(projectTitle));
    }

        /** Student submits a request and returns the new request id. */
    protected Long studentSubmitsRequest(Long projectId, String notes) throws Exception {
        mockMvc.perform(post("/api/student/requests")
                        .header("Authorization", studentAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "projectId", projectId,
                                "preferenceRank", 1,
                                "notes", notes))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult requestList = mockMvc.perform(get("/api/student/requests")
                        .header("Authorization", studentAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].requestStatus").value("PENDING"))
                .andReturn();

        return readFirstRequestId(requestList);
    }

        /** Teacher sees the request in the pending list. */
    protected void teacherCanSeePendingRequest(Long requestId) throws Exception {
        mockMvc.perform(get("/api/teacher/requests")
                        .header("Authorization", teacherAuthorization)
                        .param("status", "PENDING")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].requestStatus", requestId))
                        .value(hasItem("PENDING")));
    }

        /** Teacher accepts the request and sees the project become closed. */
    protected void teacherApprovesRequestAndSeesClosedProject(Long projectId,
                                                               Long requestId,
                                                               String decisionComment) throws Exception {
        mockMvc.perform(put("/api/teacher/requests/{requestId}/review", requestId)
                        .header("Authorization", teacherAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "requestStatus", "ACCEPTED",
                                "decisionComment", decisionComment))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/teacher/projects/{projectId}", projectId)
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.title").value(projectTitle))
                .andExpect(jsonPath("$.data.projectStatus").value("CLOSED"));

        mockMvc.perform(get("/api/teacher/requests")
                        .header("Authorization", teacherAuthorization)
                        .param("status", "ACCEPTED")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].requestStatus", requestId))
                        .value(hasItem("ACCEPTED")));
    }

        /** Student sees the accepted request and its history. */
    protected void studentCanReviewAcceptedRequestAndHistory(Long requestId) throws Exception {
        mockMvc.perform(get("/api/student/requests")
                        .header("Authorization", studentAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.records[0].requestId").value(requestId))
                .andExpect(jsonPath("$.data.records[0].requestStatus").value("ACCEPTED"));

        mockMvc.perform(get("/api/student/request-history/{requestId}", requestId)
                        .header("Authorization", studentAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0].newStatus").value("PENDING"))
                .andExpect(jsonPath("$.data[1].newStatus").value("ACCEPTED"));
    }

        /** Admin checks project, request, and history audit records. */
    protected void adminCanAuditCompletedWorkflow(Long projectId, Long requestId) throws Exception {
        mockMvc.perform(get("/api/admin/records/projects")
                        .header("Authorization", adminAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.projectId == %d)].projectStatus", projectId))
                        .value(hasItem("CLOSED")));

        mockMvc.perform(get("/api/admin/records/requests")
                        .header("Authorization", adminAuthorization)
                        .param("status", "ACCEPTED")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].requestStatus", requestId))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/records/request-history")
                        .header("Authorization", adminAuthorization)
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath(String.format("$.data.records[?(@.requestId == %d)].newStatus", requestId))
                        .value(hasItem("ACCEPTED")));

        mockMvc.perform(get("/api/admin/projects/{projectId}/tags", projectId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagA.getTagName())))
                .andExpect(jsonPath("$.data[*].tagName").value(hasItem(tagB.getTagName())));
    }

    private Long readFirstRequestId(MvcResult result) throws Exception {
                JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                JsonNode requestList = data.has("records") ? data.path("records") : data;
        return requestList.get(0).path("requestId").asLong();
    }
}
