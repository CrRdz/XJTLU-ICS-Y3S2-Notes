package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_status_history")
@Getter
@Setter
@NoArgsConstructor
public class ProjectStatusHistory {
    /** Primary key of the project status history record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "history_id")
    private Long historyId;

    /** Project whose status changed. */
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    /** Previous status value before the change. */
    @Column(name = "old_status")
    private String oldStatus;

    /** New status value after the change. */
    @Column(name = "new_status")
    private String newStatus;

    /** Teacher who made the status change. */
    @ManyToOne
    @JoinColumn(name = "changed_by", referencedColumnName = "teacher_id")
    private TeacherProfile changedBy;

    /** Optional remark explaining the change. */
    @Column(name = "remark")
    private String remark;

    /** Time when the status change happened. */
    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Builder
    public ProjectStatusHistory(Project project, String oldStatus, String newStatus,
                                TeacherProfile changedBy, String remark, LocalDateTime changedAt) {
        this.project = project;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remark = remark;
        this.changedAt = changedAt;
    }
}