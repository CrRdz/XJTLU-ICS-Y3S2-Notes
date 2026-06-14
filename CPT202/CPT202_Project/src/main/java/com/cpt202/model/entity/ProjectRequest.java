package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_request")
@Getter
@Setter
@NoArgsConstructor
public class ProjectRequest {
    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        WITHDRAWN
    }

    /** Primary key of the project request record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "request_id")
    private Long requestId;

    /** Project that the student is applying for. */
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    /** Student who submitted the request. */
    @ManyToOne
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private StudentProfile student;

    /** Teacher who reviewed the request, if applicable. */
    @ManyToOne
    @JoinColumn(name = "reviewed_by", referencedColumnName = "teacher_id")
    private TeacherProfile reviewedBy;

    /** Ranking of this request among the student's preferences. */
    @Column(name = "preference_rank")
    private Integer preferenceRank;

    /** Free-text notes submitted with the request. */
    @Column(name = "notes")
    private String notes;

    /** Current review status of the request. */
    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    /** Review decision comment from the teacher. */
    @Column(name = "decision_comment")
    private String decisionComment;

    /** Time when the request was submitted. */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /** Time when the request was reviewed. */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /** Time when the request was withdrawn by the student. */
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    /** Timestamp when the request record was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ProjectRequest(Project project, StudentProfile student, TeacherProfile reviewedBy,
                          Integer preferenceRank, String notes, RequestStatus requestStatus,
                          String decisionComment, LocalDateTime submittedAt,
                          LocalDateTime reviewedAt, LocalDateTime withdrawnAt,
                          LocalDateTime updatedAt) {
        this.project = project;
        this.student = student;
        this.reviewedBy = reviewedBy;
        this.preferenceRank = preferenceRank;
        this.notes = notes;
        this.requestStatus = requestStatus;
        this.decisionComment = decisionComment;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.withdrawnAt = withdrawnAt;
        this.updatedAt = updatedAt;
    }
}