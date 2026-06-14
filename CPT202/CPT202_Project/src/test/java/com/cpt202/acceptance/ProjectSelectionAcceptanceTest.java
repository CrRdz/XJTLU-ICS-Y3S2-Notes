package com.cpt202.acceptance;

import com.cpt202.support.ProjectSelectionWorkflowSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Acceptance stories for project selection. */
@Cpt202AcceptanceTest
class ProjectSelectionAcceptanceTest extends ProjectSelectionWorkflowSupport {

    @BeforeEach
    void setUp() {
        initializeProjectSelectionActors("Acceptance");
    }

    /** Student discovers a project and submits a request. */
    @Test
    void studentCanDiscoverProjectAndSubmitPreference() throws Exception {
        Long projectId = teacherPublishesProject("Acceptance scenario project.", 2);
        teacherBindsProjectTags(projectId);
        studentCanDiscoverProject(projectId);
        studentSubmitsRequest(projectId, "Acceptance request from student.");
    }

    /** Teacher accepts a request and sees the project close. */
    @Test
    void teacherCanAcceptARequestAndCloseTheProject() throws Exception {
        Long projectId = teacherPublishesProject("Acceptance scenario project.", 1);
        teacherBindsProjectTags(projectId);

        Long requestId = studentSubmitsRequest(projectId, "Acceptance request from student.");
        teacherCanSeePendingRequest(requestId);
        teacherApprovesRequestAndSeesClosedProject(projectId, requestId, "Accepted in acceptance scenario.");
    }

    /** Admin audits the completed workflow. */
    @Test
    void administratorCanAuditACompletedSelectionWorkflow() throws Exception {
        Long projectId = teacherPublishesProject("Acceptance scenario project.", 1);
        teacherBindsProjectTags(projectId);

        Long requestId = studentSubmitsRequest(projectId, "Acceptance request from student.");
        teacherApprovesRequestAndSeesClosedProject(projectId, requestId, "Accepted in acceptance scenario.");
        adminCanAuditCompletedWorkflow(projectId, requestId);
    }
}