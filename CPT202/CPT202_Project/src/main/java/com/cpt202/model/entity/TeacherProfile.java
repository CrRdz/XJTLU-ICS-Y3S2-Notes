package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_profile")
@Getter
@Setter
@NoArgsConstructor
public class TeacherProfile {
    /** Primary key shared with the related user account. */
    @Id
    @Setter(AccessLevel.NONE)
    @Column(name = "teacher_id")
    private Long teacherId;

    /** Institutional staff number. */
    @Column(name = "staff_no")
    private String staffNo;

    /** Department that the teacher belongs to. */
    @Column(name = "department")
    private String department;

    /** Academic or professional title of the teacher. */
    @Column(name = "title")
    private String title;

    /** Main research area of the teacher. */
    @Column(name = "research_area")
    private String researchArea;

    /** Office location of the teacher. */
    @Column(name = "office")
    private String office;

    /** Timestamp when the profile was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** User account linked to this teacher profile. */
    @OneToOne
    @MapsId
    @JoinColumn(name = "teacher_id", referencedColumnName = "user_id")
    private User user;

    @Builder
    public TeacherProfile(String staffNo, String department, String title, String researchArea, String office, LocalDateTime updatedAt, User user) {
        this.staffNo = staffNo;
        this.department = department;
        this.title = title;
        this.researchArea = researchArea;
        this.office = office;
        this.updatedAt = updatedAt;
        this.user = user;
    }
}