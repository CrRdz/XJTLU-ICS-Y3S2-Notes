package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    /** Primary key of the tag record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "tag_id")
    private Long tagId;

    /** Display name of the tag. */
    @Column(name = "tag_name")
    private String tagName;

    /** Detailed explanation of the tag. */
    @Column(name = "description")
    private String description;

    /** Timestamp when the tag was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the tag was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
