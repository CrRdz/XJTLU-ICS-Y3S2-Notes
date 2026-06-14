package com.cpt202.integration;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.support.ProjectSelectionWorkflowSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Scenario-level integration test for the main business flow.
 *
 * The current backend exposes no administrator archive endpoint. Even though the
 * ProjectStatus enum still contains ARCHIVED, the teacher status API explicitly
 * rejects switching to it and the admin controller surface has no replacement.
 *
 * To keep the scenario executable against the real code, the workflow therefore
 * treats the current terminal state as follows:
 * 1. teacher creates a project with a single seat
 * 2. student submits a request
 * 3. teacher accepts that request
 * 4. the system automatically closes the project because capacity is full
 * 5. admin audits project/request/history records for the finished workflow
 *
 * Each business step is extracted into its own helper method so future changes
 * can insert, remove or rewrite a single step without turning the scenario test
 * into one large opaque method.
 */
@Cpt202IntegrationTest
class ProjectLifecycleIntegrationTest extends ProjectSelectionWorkflowSupport {

    /** Creates one teacher-student-admin scenario fixture before each run. */
    @BeforeEach
    void setUp() {
        initializeProjectSelectionActors("Scenario");
    }

    /**
     * Full role-based lifecycle test.
     *
     * The method itself stays compact and readable by delegating each role-owned
     * action to a helper named after the business step it represents.
     */
    @Test
    void teacherStudentAdminLifecycleFormsAuditableClosedProject() throws Exception {
        Long projectId = teacherPublishesProject("Scenario project used for workflow integration testing.", 1);
        Project createdProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(createdProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.AVAILABLE);
        teacherBindsProjectTags(projectId);
        studentCanDiscoverProject(projectId);

        Long requestId = studentSubmitsRequest(projectId, "Scenario request from student.");

        Project requestedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(requestedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.REQUESTED);
        teacherCanSeePendingRequest(requestId);
        teacherApprovesRequestAndSeesClosedProject(projectId, requestId, "Accepted in scenario workflow.");

        ProjectRequest acceptedRequest = projectRequestRepository.findById(requestId).orElseThrow();
        Project closedProject = projectRepository.findById(projectId).orElseThrow();
        assertThat(acceptedRequest.getRequestStatus()).isEqualTo(ProjectRequest.RequestStatus.ACCEPTED);
        assertThat(closedProject.getProjectStatus()).isEqualTo(Project.ProjectStatus.CLOSED);
        assertThat(closedProject.getCurrentAgreedCount()).isEqualTo(1);

        studentCanReviewAcceptedRequestAndHistory(requestId);
        adminCanAuditCompletedWorkflow(projectId, requestId);
    }
}