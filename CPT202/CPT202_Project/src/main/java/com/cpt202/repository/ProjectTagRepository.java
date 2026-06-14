package com.cpt202.repository;

import com.cpt202.model.entity.ProjectTag;
import com.cpt202.model.entity.ProjectTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTagRepository extends JpaRepository<ProjectTag, ProjectTagId> {

    List<ProjectTag> findByProject_ProjectIdOrderByTag_TagIdAsc(Long projectId);

    void deleteByProject_ProjectId(Long projectId);
}
