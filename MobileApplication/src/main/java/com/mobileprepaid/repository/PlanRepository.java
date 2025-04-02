package com.mobileprepaid.repository;

import com.mobileprepaid.entities.Plan;
import com.mobileprepaid.enums.PlanStatus;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long>, JpaSpecificationExecutor<Plan> {

    // Fetch ACTIVE plans by Name
    Page<Plan> findByNameContainingIgnoreCaseAndStatus(String name, PlanStatus status, Pageable pageable);

    // Fetch ACTIVE plans by Category
    @Query("SELECT p FROM Plan p WHERE p.category.name = :categoryName AND p.status = :status")
    Page<Plan> findByCategoryNameAndStatus(@Param("categoryName") String categoryName, @Param("status") PlanStatus status, Pageable pageable);
    // Fetch ACTIVE plans by Data Limit
    Page<Plan> findByDataLimitContainingIgnoreCaseAndStatus(String dataLimit, PlanStatus status, Pageable pageable);

    // Fetch ACTIVE plans by Validity
    Page<Plan> findByValidityAndStatus(Integer validity, PlanStatus status, Pageable pageable);

    // ✅ Get all ACTIVE plans for Subscribers
    Page<Plan> findByStatus(PlanStatus status, Pageable pageable);

    // ✅ Get all plans (Admin)
    Page<Plan> findAll(Pageable pageable);

    // ✅ Sorting by Price for ACTIVE Plans
    Page<Plan> findByStatusOrderByPriceAsc(PlanStatus status, Pageable pageable);
    Page<Plan> findByStatusOrderByPriceDesc(PlanStatus status, Pageable pageable);
}
