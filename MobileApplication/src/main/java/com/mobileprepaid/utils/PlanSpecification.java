package com.mobileprepaid.utils;

import com.mobileprepaid.entities.Plan;
import com.mobileprepaid.enums.PlanStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PlanSpecification {
    public static Specification<Plan> filterPlans(String name, String category, String dataLimit, Integer validity) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Filter by Name
            if (name != null && !name.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            // Filter by Category Name (fixed to join Category entity)
            if (category != null && !category.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.like(criteriaBuilder.lower(root.join("category").get("name")), "%" + category.toLowerCase() + "%"));
            }

            // Filter by Data Limit
            if (dataLimit != null && !dataLimit.trim().isEmpty()) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("dataLimit")), "%" + dataLimit.toLowerCase() + "%"));
            }

            // Filter by Validity
            if (validity != null) {
                predicate = criteriaBuilder.and(predicate, 
                    criteriaBuilder.equal(root.get("validity"), validity));
            }

            // Show only ACTIVE plans for Subscribers
            predicate = criteriaBuilder.and(predicate, 
                criteriaBuilder.equal(root.get("status"), PlanStatus.ACTIVE));

            return predicate;
        };
    }
}