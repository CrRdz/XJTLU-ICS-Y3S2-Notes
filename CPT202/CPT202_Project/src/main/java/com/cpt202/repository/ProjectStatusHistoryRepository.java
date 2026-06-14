package com.cpt202.repository;

import com.cpt202.model.entity.ProjectStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectStatusHistoryRepository extends JpaRepository<ProjectStatusHistory, Long> {
}
