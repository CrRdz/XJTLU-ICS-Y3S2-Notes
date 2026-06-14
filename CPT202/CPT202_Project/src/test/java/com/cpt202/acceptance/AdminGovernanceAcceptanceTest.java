package com.cpt202.acceptance;

import com.cpt202.constant.MessageConstants;
import com.cpt202.integration.IntegrationTestSupport;
import com.cpt202.model.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Acceptance stories for administrator governance operations. */
@Cpt202AcceptanceTest
class AdminGovernanceAcceptanceTest extends IntegrationTestSupport {

    private User adminUser;
    private User managedStudentUser;
        private User teacherUser;
    private String adminAuthorization;
        private String teacherAuthorization;

    /** Creates the administrator and one managed student account. */
    @BeforeEach
    void setUp() {
        String suffix = uniqueSuffix();

        adminUser = createAdminUser(
                "acceptance-governance-admin-" + suffix,
                "acceptance-governance-admin-" + suffix + "@example.com",
                "Acceptance Governance Admin " + suffix
        );
        managedStudentUser = createUser(
                "acceptance-governance-student-" + suffix,
                "acceptance-governance-student-" + suffix + "@example.com",
                "Acceptance Governance Student " + suffix,
                User.UserRole.STUDENT
        );
        teacherUser = createUser(
                "acceptance-governance-teacher-" + suffix,
                "acceptance-governance-teacher-" + suffix + "@example.com",
                "Acceptance Governance Teacher " + suffix,
                User.UserRole.TEACHER
        );

        adminAuthorization = authorizationFor(adminUser);
        teacherAuthorization = authorizationFor(teacherUser);
    }

    /** Administrator can create, update, inspect, and delete a category. */
    @Test
    void administratorCanMaintainCategoryCatalog() throws Exception {
        String suffix = uniqueSuffix();
        String categoryName = "AcceptanceManagedCategory" + suffix;
        String updatedCategoryName = categoryName + "Updated";

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName,
                                "description", "Acceptance governance category"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult categoryListResult = mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        long categoryId = findIdByName(categoryListResult, "categoryId", "categoryName", categoryName);

        mockMvc.perform(get("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.categoryName").value(categoryName))
                .andExpect(jsonPath("$.data.description").value("Acceptance governance category"));

        mockMvc.perform(put("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", updatedCategoryName,
                                "description", "Updated governance category"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.categoryName").value(updatedCategoryName))
                .andExpect(jsonPath("$.data.description").value("Updated governance category"));

        mockMvc.perform(delete("/api/admin/categories/{categoryId}", categoryId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult deletedCategoryListResult = mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        assertThat(containsEntry(deletedCategoryListResult, "categoryName", updatedCategoryName)).isFalse();
    }

    /** Administrator can create, update, inspect, and delete a tag. */
    @Test
    void administratorCanMaintainTagCatalog() throws Exception {
        String suffix = uniqueSuffix();
        String tagName = "AcceptanceManagedTag" + suffix;
        String updatedTagName = tagName + "Updated";

        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName,
                                "description", "Acceptance governance tag"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult tagListResult = mockMvc.perform(get("/api/admin/tags")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        long tagId = findIdByName(tagListResult, "tagId", "tagName", tagName);

        mockMvc.perform(get("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.tagName").value(tagName))
                .andExpect(jsonPath("$.data.description").value("Acceptance governance tag"));

        mockMvc.perform(put("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", updatedTagName,
                                "description", "Updated governance tag"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(get("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.tagName").value(updatedTagName))
                .andExpect(jsonPath("$.data.description").value("Updated governance tag"));

        mockMvc.perform(delete("/api/admin/tags/{tagId}", tagId)
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult deletedTagListResult = mockMvc.perform(get("/api/admin/tags")
                        .header("Authorization", adminAuthorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        assertThat(containsEntry(deletedTagListResult, "tagName", updatedTagName)).isFalse();
    }

    /** Administrator can filter users and change a student's account status. */
    @Test
    void administratorCanFilterUsersAndDisableAnAccount() throws Exception {
        MvcResult activeStudentsResult = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminAuthorization)
                        .param("role", "STUDENT")
                        .param("accountStatus", "ACTIVE")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        assertThat(findUserStatus(activeStudentsResult, managedStudentUser.getUserId())).isEqualTo("ACTIVE");

        mockMvc.perform(put("/api/admin/users/{userId}/status", managedStudentUser.getUserId())
                        .header("Authorization", adminAuthorization)
                        .param("accountStatus", "DISABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        MvcResult disabledStudentsResult = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminAuthorization)
                        .param("role", "STUDENT")
                        .param("accountStatus", "DISABLED")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        assertThat(findUserStatus(disabledStudentsResult, managedStudentUser.getUserId())).isEqualTo("DISABLED");

        MvcResult refreshedActiveStudentsResult = mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", adminAuthorization)
                        .param("role", "STUDENT")
                        .param("accountStatus", "ACTIVE")
                        .param("pageNum", "1")
                        .param("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andReturn();

        assertThat(findUserStatus(refreshedActiveStudentsResult, managedStudentUser.getUserId())).isNull();
    }

    /** Administrator cannot create a category with a duplicate trimmed name. */
    @Test
    void administratorCannotCreateDuplicateCategoryName() throws Exception {
        String suffix = uniqueSuffix();
        String categoryName = "AcceptanceDuplicateCategory" + suffix;

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", categoryName,
                                "description", "First category instance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/api/admin/categories")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "categoryName", "  " + categoryName + "  ",
                                "description", "Duplicate category instance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstants.CATEGORY_NAME_EXISTS));
    }

    /** Administrator cannot create a tag with a duplicate trimmed name. */
    @Test
    void administratorCannotCreateDuplicateTagName() throws Exception {
        String suffix = uniqueSuffix();
        String tagName = "AcceptanceDuplicateTag" + suffix;

        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", tagName,
                                "description", "First tag instance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        mockMvc.perform(post("/api/admin/tags")
                        .header("Authorization", adminAuthorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tagName", "  " + tagName + "  ",
                                "description", "Duplicate tag instance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstants.TAG_NAME_EXISTS));
    }

    /** Only administrators can access governance endpoints. */
    @Test
    void nonAdministratorsCannotAccessGovernanceEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(MessageConstants.INVALID_BEARER_TOKEN));

        mockMvc.perform(get("/api/admin/categories")
                        .header("Authorization", teacherAuthorization))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(String.format(
                        MessageConstants.ROLE_REQUIRED_TEMPLATE,
                        teacherUser.getRole(),
                        User.UserRole.ADMIN
                )));
    }

    /** Finds the ID of the item whose display name matches the expected value. */
    private long findIdByName(MvcResult result, String idField, String nameField, String expectedName) throws Exception {
        Iterator<JsonNode> items = readData(result).elements();
        while (items.hasNext()) {
            JsonNode item = items.next();
            if (expectedName.equals(item.path(nameField).asText())) {
                return item.path(idField).asLong();
            }
        }
        throw new AssertionError("Could not find item named " + expectedName);
    }

    /** Checks whether a list response still contains an item with the supplied field value. */
    private boolean containsEntry(MvcResult result, String fieldName, String expectedValue) throws Exception {
        Iterator<JsonNode> items = readData(result).elements();
        while (items.hasNext()) {
            if (expectedValue.equals(items.next().path(fieldName).asText())) {
                return true;
            }
        }
        return false;
    }

    /** Reads one user's account status from a list response. */
    private String findUserStatus(MvcResult result, Long userId) throws Exception {
        Iterator<JsonNode> items = readData(result).elements();
        while (items.hasNext()) {
            JsonNode item = items.next();
            if (userId.equals(item.path("userId").asLong())) {
                return item.path("accountStatus").asText();
            }
        }
        return null;
    }

    /** Reads the wrapped data payload from a MockMvc response. */
    private JsonNode readData(MvcResult result) throws Exception {
                JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
                return data.has("records") ? data.path("records") : data;
    }
}