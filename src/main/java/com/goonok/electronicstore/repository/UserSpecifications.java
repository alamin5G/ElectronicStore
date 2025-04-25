package com.goonok.electronicstore.repository;

import com.goonok.electronicstore.dto.UserSearchCriteria;
import com.goonok.electronicstore.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecifications {
    public static Specification<User> withSearchCriteria(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getNameContains() != null) {
                predicates.add(cb.like(cb.lower(root.get("fullName")),
                        "%" + criteria.getNameContains().toLowerCase() + "%"));
            }

            if (criteria.getEmailContains() != null) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + criteria.getEmailContains().toLowerCase() + "%"));
            }

            if (criteria.getEnabled() != null) {
                predicates.add(cb.equal(root.get("isEnabled"), criteria.getEnabled()));
            }

            if (criteria.getVerified() != null) {
                predicates.add(cb.equal(root.get("isVerified"), criteria.getVerified()));
            }

            if (criteria.getCreatedDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                        criteria.getCreatedDateStart()));
            }

            if (criteria.getCreatedDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                        criteria.getCreatedDateEnd()));
            }

            // Add more criteria as needed

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}