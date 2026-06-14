package com.cpt202.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_tag")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTag {
    /** Composite primary key for the project-tag relation. */
    @EmbeddedId
    @Builder.Default
    private ProjectTagId id = new ProjectTagId();

    /** Project participating in the relation. */
    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    /** Tag participating in the relation. */
    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id")
    private Tag tag;
}