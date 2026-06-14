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
@Table(name = "category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    /** Primary key of the category record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "category_id")
    private Long categoryId;

    /** Display name of the category. */
    @Column(name = "category_name")
    private String categoryName;

    /** Detailed description of the category. */
    @Column(name = "description")
    private String description;

    /** Timestamp when the category was created. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Timestamp when the category was last updated. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
