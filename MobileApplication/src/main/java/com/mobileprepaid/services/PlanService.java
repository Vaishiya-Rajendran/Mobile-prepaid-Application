package com.mobileprepaid.services;

import com.mobileprepaid.entities.Plan;
import com.mobileprepaid.enums.PlanStatus;
import com.mobileprepaid.repository.PlanRepository;
import com.mobileprepaid.utils.PlanSpecification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    // ✅ Fetch only ACTIVE plans for subscribers
//    public Page<Plan> getFilteredActivePlans(String name, String category, String dataLimit, Integer validity, Pageable pageable) {
//        Specification<Plan> spec = PlanSpecification.filterPlans(name, category, dataLimit, validity);
//        return planRepository.findAll(spec, pageable);
//    }
    
 // Fetch only ACTIVE plans for subscribers
    public Page<Plan> getFilteredActivePlans(String name, String category, String dataLimit, Integer validity, Pageable pageable) {
        // Use filterPlans directly, as it already includes the ACTIVE status filter
        Specification<Plan> spec = PlanSpecification.filterPlans(name, category, dataLimit, validity);
        return planRepository.findAll(spec, pageable);
    }
    public Page<Plan> getPlansByCategory(String category, Pageable pageable) {
        return planRepository.findByCategoryNameAndStatus(category, PlanStatus.ACTIVE, pageable);
    }
    
    public Page<Plan> getPlansByName(String name, Pageable pageable) {
        return planRepository.findByNameContainingIgnoreCaseAndStatus(name, PlanStatus.ACTIVE, pageable);
    }

    public Page<Plan> getPlansByDataLimit(String dataLimit, Pageable pageable) {
        return planRepository.findByDataLimitContainingIgnoreCaseAndStatus(dataLimit, PlanStatus.ACTIVE, pageable);
    }

    public Page<Plan> getPlansByValidity(Integer validity, Pageable pageable) {
        return planRepository.findByValidityAndStatus(validity, PlanStatus.ACTIVE, pageable);
    }
    // ✅ Fetch ALL plans for Admin (ACTIVE + INACTIVE)
    public Page<Plan> getAllPlans(Pageable pageable) {
        return planRepository.findAll(pageable);
    }

    // ✅ Create or Update Plan
    public Plan savePlan(Plan plan) {
        if (plan.getName() == null || plan.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan name cannot be empty");
        }
        if (plan.getCategory() == null || plan.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan must have a category and valid status");
        }
        return planRepository.save(plan);
    }

    // ✅ Get a plan by ID
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
    }

    // ✅ Update Plan
    public Plan updatePlan(Long id, Plan updatedPlan) {
        Plan existingPlan = planRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        existingPlan.setName(updatedPlan.getName());
        existingPlan.setValidity(updatedPlan.getValidity());
        existingPlan.setDataLimit(updatedPlan.getDataLimit());
        existingPlan.setCallLimit(updatedPlan.getCallLimit());
        existingPlan.setSmsLimit(updatedPlan.getSmsLimit());
        existingPlan.setPrice(updatedPlan.getPrice());
        existingPlan.setCategory(updatedPlan.getCategory());
        existingPlan.setStatus(updatedPlan.getStatus());
        return planRepository.save(existingPlan);
    }

    // ✅ Delete a plan
    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found");
        }
        planRepository.deleteById(id);
    }

    // ✅ Update Plan Status (ACTIVE ⇄ INACTIVE)
    public Plan updatePlanStatus(Long planId, String status) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        try {
            PlanStatus newStatus = PlanStatus.valueOf(status.toUpperCase()); // Convert String to Enum
            plan.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Allowed values: ACTIVE or INACTIVE");
        }

        return planRepository.save(plan);
    }
}
