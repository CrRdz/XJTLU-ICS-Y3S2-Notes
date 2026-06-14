package com.cpt202.repository.specification;

import com.cpt202.model.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> byRoleAndStatus(User.UserRole role, String accountStatus) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (accountStatus != null && !accountStatus.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("accountStatus"), accountStatus));
            }
            query.orderBy(criteriaBuilder.asc(root.get("userId")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
