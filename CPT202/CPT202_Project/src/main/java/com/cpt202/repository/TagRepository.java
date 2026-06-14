package com.cpt202.repository;

import com.cpt202.model.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByTagNameIgnoreCase(String tagName);

    boolean existsByTagNameIgnoreCaseAndTagIdNot(String tagName, Long tagId);
}
