package com.cpt202.repository.specification;

import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectTag;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> studentQuery(String keyword,
                                                      Long categoryId,
                                                      Project.ProjectStatus status,
                                                      List<Long> tagIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("projectStatus"), status));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), categoryId));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), pattern);
                Predicate topicLike = cb.like(cb.lower(root.get("topicArea")), pattern);
                Predicate skillsLike = cb.like(cb.lower(root.get("requiredSkills")), pattern);
                predicates.add(cb.or(titleLike, descLike, topicLike, skillsLike));
            }

            if (tagIds != null && !tagIds.isEmpty()) {
                // 必须命中全部 tag：子查询统计该项目命中的 tag 数等于传入数量。
                Subquery<Long> sub = query.subquery(Long.class);
                Root<ProjectTag> ptRoot = sub.from(ProjectTag.class);
                sub.select(cb.countDistinct(ptRoot.get("tag").get("tagId")))
                        .where(cb.equal(ptRoot.get("project"), root),
                                ptRoot.get("tag").get("tagId").in(tagIds));
                predicates.add(cb.equal(sub, (long) tagIds.size()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
