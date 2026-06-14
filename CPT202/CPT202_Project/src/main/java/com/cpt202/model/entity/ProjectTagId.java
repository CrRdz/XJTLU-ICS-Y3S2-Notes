package com.cpt202.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTagId implements Serializable {
    /** Project identifier in the composite key. */
    @Column(name = "project_id")
    private Long projectId;

    /** Tag identifier in the composite key. */
    @Column(name = "tag_id")
    private Long tagId;
}