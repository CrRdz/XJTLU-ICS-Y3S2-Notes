package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    /** Available system roles for a user account. */
    public enum UserRole {
        ADMIN,
        TEACHER,
        STUDENT
    }

    /** Primary key of the user record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** Unique login name of the user. */
    @Column(name = "username")
    private String username;

    /** Persisted password hash of the user. */
    @Column(name = "password_hash")
    private String passwordHash;

    /** Email address associated with the account. */
    @Column(name = "email")
    private String email;

    /** Full display name of the user. */
    @Column(name = "full_name")
    private String fullName;

    /** Role assigned to the user account. */
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    /** Current account status label. */
    @Column(name = "account_status")
    private String accountStatus;

    /** Whether TOTP-based two-factor authentication is enabled. */
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;

    /** Base32 secret used for TOTP verification. */
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    /** Timestamp when the user record was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the user record was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Student profile linked to this user, if the role is student. */
    @OneToOne(mappedBy = "user")
    private StudentProfile studentProfile;

    /** Teacher profile linked to this user, if the role is teacher. */
    @OneToOne(mappedBy = "user")
    private TeacherProfile teacherProfile;

    @Builder
    public User(Long userId, String username, String passwordHash, String email, String fullName, UserRole role, String accountStatus, Boolean twoFactorEnabled, String twoFactorSecret, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.accountStatus = accountStatus;
        this.twoFactorEnabled = twoFactorEnabled;
        this.twoFactorSecret = twoFactorSecret;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void setStudentProfile(StudentProfile studentProfile) {
        this.studentProfile = studentProfile;
        if (studentProfile != null && studentProfile.getUser() != this) {
            studentProfile.setUser(this);
        }
    }

    public void setTeacherProfile(TeacherProfile teacherProfile) {
        this.teacherProfile = teacherProfile;
        if (teacherProfile != null && teacherProfile.getUser() != this) {
            teacherProfile.setUser(this);
        }
    }
}
