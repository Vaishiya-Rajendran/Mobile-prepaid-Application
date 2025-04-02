package com.mobileprepaid.controllers;

import com.mobileprepaid.dto.NotificationRequest;
import com.mobileprepaid.dto.ResponseMessage;
import com.mobileprepaid.dto.TransactionResponseDTO;
import com.mobileprepaid.entities.*;
import com.mobileprepaid.enums.SubscriberStatus;
import com.mobileprepaid.repository.SubscriberRepository;
import com.mobileprepaid.repository.SubscriberLoginRepository;
import com.mobileprepaid.services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SubscriberLoginRepository subscriberLoginRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SubscriberPlanPaymentService subscriberPlanPaymentService;

    @Autowired
    private NotificationService notificationService;
    
    
 // =========================== DASHBOARD SUMMARY ===========================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            // Active Users
            long activeUsers = subscriberRepository.countByStatus(SubscriberStatus.ACTIVE);
            summary.put("activeUsers", activeUsers);

            // Total Recharges Today
            LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            Page<TransactionResponseDTO> transactions = subscriberPlanPaymentService.getAllTransactions(
                PageRequest.of(0, Integer.MAX_VALUE)); // Fetch all for simplicity
            long totalRechargesToday = transactions.stream()
                .filter(t -> t.getCreatedAt().isAfter(startOfDay) && t.getCreatedAt().isBefore(endOfDay))
                .count();
            summary.put("totalRechargesToday", totalRechargesToday);

            // Total Revenue Today
            double totalRevenueToday = transactions.stream()
                .filter(t -> t.getCreatedAt().isAfter(startOfDay) && t.getCreatedAt().isBefore(endOfDay))
                .mapToDouble(TransactionResponseDTO::getAmount)
                .sum();
            summary.put("totalRevenueToday", totalRevenueToday);

            // Pending Complaints & Requests (placeholder)
            summary.put("pendingIssues", 0); // Replace with actual logic if you have a complaints entity

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch dashboard data", e);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/plans/expiring")
    public ResponseEntity<List<Map<String, Object>>> getExpiringPlans() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);

        // Using Transaction data since SubscriberPlanPayment entity doesn't exist
        Page<TransactionResponseDTO> transactions = subscriberPlanPaymentService.getAllTransactions(
            PageRequest.of(0, Integer.MAX_VALUE));

        try {
            List<Map<String, Object>> expiringPlans = transactions.stream()
                .filter(t -> {
                    // Calculate expiry date based on transaction creation date and plan validity
                    LocalDate expiryDate = t.getCreatedAt().toLocalDate().plusDays(t.getPlan().getValidity());
                    return expiryDate != null && !expiryDate.isBefore(today) && !expiryDate.isAfter(threeDaysFromNow);
                })
                .map(t -> {
                    Map<String, Object> planData = new HashMap<>();
                    planData.put("name", t.getUsername());
                    planData.put("mobileNumber", t.getPhoneNumber());
                    planData.put("expiryDate", t.getCreatedAt().toLocalDate()
                        .plusDays(t.getPlan().getValidity()).toString());
                    return planData;
                })
                .toList();

            return ResponseEntity.ok(expiringPlans);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch expiring plans", e);
        }
    }
    
    // =========================== SUBSCRIBER MANAGEMENT ===========================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getsubscriber/{id}")
    public ResponseEntity<Subscriber> getSubscriberById(@PathVariable Long id) {
        Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found"));
        return ResponseEntity.ok(subscriber);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/verify-subscriber/{id}")
    public ResponseEntity<Map<String, String>> verifySubscriber(
            @PathVariable Long id,
            @RequestParam SubscriberStatus status) {
        if (status == SubscriberStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status update");
        }
        Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found"));
        subscriber.setStatus(status);
        subscriberRepository.save(subscriber);

        if (status == SubscriberStatus.ACTIVE) {
            SubscriberLogin subscriberLogin = subscriberLoginRepository.findByPhoneNumber(subscriber.getPhoneNumber())
                    .orElse(new SubscriberLogin());
            subscriberLogin.setSubscriber(subscriber);
            subscriberLogin.setPhoneNumber(subscriber.getPhoneNumber());
            subscriberLoginRepository.save(subscriberLogin);
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "Subscriber status updated to " + status));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(value = "/getsubscribers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Subscriber>> getSubscribers(
            @RequestParam(required = false) SubscriberStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Subscriber> subscribers = (status != null)
                ? subscriberRepository.findByStatus(status, pageable)
                : subscriberRepository.findAll(pageable);
        return ResponseEntity.ok(subscribers);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/deletesubscriber/{id}")
    public ResponseEntity<Map<String, String>> deleteSubscriber(@PathVariable Long id) {
        Subscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscriber not found"));
        subscriber.setStatus(SubscriberStatus.INACTIVE);
        subscriberRepository.save(subscriber);
        return ResponseEntity.ok(Collections.singletonMap("message", "Subscriber deactivated successfully"));
    }

    // =========================== CATEGORY MANAGEMENT ===========================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/categories/name/{name}")
    public ResponseEntity<Category> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updatedCategory));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // =========================== PLAN MANAGEMENT ===========================
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/plans")
    public ResponseEntity<Page<Plan>> getAllPlans(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String dataLimit,
            @RequestParam(required = false) Integer validity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<Plan> plans;
        if (name != null && !name.isEmpty()) {
            plans = planService.getPlansByName(name, pageable);
        } else if (category != null && !category.isEmpty()) {
            // Use corrected repository method for category filtering
            plans = planService.getPlansByCategory(category, pageable);
        } else if (dataLimit != null && !dataLimit.isEmpty()) {
            plans = planService.getPlansByDataLimit(dataLimit, pageable);
        } else if (validity != null) {
            plans = planService.getPlansByValidity(validity, pageable);
        } else {
            plans = planService.getAllPlans(pageable); // All plans for admin
        }

        return ResponseEntity.ok(plans);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/plans")
    public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
        return ResponseEntity.ok(planService.savePlan(plan));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/plans/{id}")
    public ResponseEntity<Plan> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/plans/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody Plan updatedPlan) {
        return ResponseEntity.ok(planService.updatePlan(id, updatedPlan));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/plans/{id}/status")
    public ResponseEntity<String> updatePlanStatus(@PathVariable Long id, @RequestParam String status) {
        Plan updatedPlan = planService.updatePlanStatus(id, status);
        return ResponseEntity.ok("Plan status updated to " + updatedPlan.getStatus());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<String> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok("Plan deleted successfully");
    }

    // =========================== RECHARGE & TRANSACTIONS ===========================
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<TransactionResponseDTO> transactions = subscriberPlanPaymentService.getAllTransactions(pageable);
        if (transactions.isEmpty()) {
            ResponseMessage responseMessage = new ResponseMessage("No Content", "No transactions found");
            return ResponseEntity.ok(responseMessage);
        }
        return ResponseEntity.ok(transactions);
    }

    // =========================== NOTIFICATIONS ===========================
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, String>> sendNotification(@RequestBody NotificationRequest request) {
        try {
            notificationService.sendNotification(request.getName(), request.getMobileNumber(), request.getMessage());
            return ResponseEntity.ok(Map.of("message", "Notification sent successfully to " + request.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send notification: " + e.getMessage()));
        }
    }
}