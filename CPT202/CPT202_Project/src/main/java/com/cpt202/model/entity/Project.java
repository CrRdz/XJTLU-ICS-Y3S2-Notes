package com.cpt202.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "project")
@Getter
@Setter
@NoArgsConstructor
public class Project {
    public enum ProjectStatus {
        AVAILABLE,
        REQUESTED,
        AGREED,
        CLOSED,
        ARCHIVED
    }

    /** Primary key of the project record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "project_id")
    private Long projectId;

    /** Teacher who publishes or owns the project. */
    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacher_id")
    private TeacherProfile teacher;

    /** Category assigned to the project. */
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    /** Title shown to students for the project. */
    @Column(name = "title")
    private String title;

    /** Full description of the project content. */
    @Column(name = "description")
    private String description;

    /** Skills expected from applicants. */
    @Column(name = "required_skills")
    private String requiredSkills;

    /** Research or business topic area of the project. */
    @Column(name = "topic_area")
    private String topicArea;

    /** Maximum number of students allowed in the project. */
    @Min(1)
    @Column(name = "max_students")
    private Integer maxStudents;

    /** Current number of students already agreed for the project. */
    @Min(0)
    @Column(name = "current_agreed_count")
    private Integer currentAgreedCount;

    /** Current lifecycle status of the project. */
    @Enumerated(EnumType.STRING)
    @Column(name = "project_status")
    private ProjectStatus projectStatus;

    /** Time when the project becomes visible or published. */
    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    /** Time when the project is closed for new requests. */
    @Column(name = "close_date")
    private LocalDateTime closeDate;

    /** Timestamp when the project record was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the project record was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Project(TeacherProfile teacher, Category category, String title, String description,
                   String requiredSkills, String topicArea, Integer maxStudents, Integer currentAgreedCount,
                   ProjectStatus projectStatus, LocalDateTime publishDate, LocalDateTime closeDate,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.teacher = teacher;
        this.category = category;
        this.title = title;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.topicArea = topicArea;
        this.maxStudents = maxStudents;
        this.currentAgreedCount = currentAgreedCount;
        this.projectStatus = projectStatus;
        this.publishDate = publishDate;
        this.closeDate = closeDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
