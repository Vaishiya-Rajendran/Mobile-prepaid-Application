//
//package com.mobileprepaid.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import com.mobileprepaid.dto.PaymentRequestDTO;
//import com.mobileprepaid.dto.ResponseMessage;
//import com.mobileprepaid.dto.SubscriberUpdateRequest;
//import com.mobileprepaid.dto.TransactionResponseDTO;
//import com.mobileprepaid.entities.Notification;
//import com.mobileprepaid.entities.Plan;
//import com.mobileprepaid.entities.Subscriber;
//import com.mobileprepaid.entities.Transaction;
//import com.mobileprepaid.repository.SubscriberRepository;
//import com.mobileprepaid.services.NotificationService;
//import com.mobileprepaid.services.PlanService;
//import com.mobileprepaid.services.SubscriberPlanPaymentService;
//import java.security.Principal;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@PreAuthorize("hasAuthority('SUBSCRIBER')")
//@RequestMapping("/subscriber")
//public class SubscriberController {
//
//    @Autowired
//    private PlanService planService;
//
//    @Autowired
//    private SubscriberRepository subscriberRepository;
//
//    @Autowired
//    private SubscriberPlanPaymentService subscriberPlanPaymentService;
//    
//    @Autowired
//    private NotificationService notificationService;
//
//    @GetMapping("/plans")
//    public ResponseEntity<?> getAllActivePlans(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String dataLimit,
//            @RequestParam(required = false) Integer validity,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "5") int size,
//            @RequestParam(defaultValue = "price") String sortBy,
//            @RequestParam(defaultValue = "asc") String sortDir) {
//
//        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//        PageRequest pageable = PageRequest.of(page, size, sort);
//
//        Page<Plan> plans = planService.getFilteredActivePlans(name, category, dataLimit, validity, pageable);
//        if (plans.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(plans);
//    }
//
//    @GetMapping("/profile")
//    public ResponseEntity<?> getProfile(Principal principal) {
//        if (principal == null || principal.getName() == null) {
//            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
//        }
//
//        String phoneNumber = principal.getName();
//        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
//        if (subscriberOpt.isEmpty()) {
//            return ResponseEntity.status(404).body(new ResponseMessage("Not Found", "Subscriber not found"));
//        }
//
//        Subscriber subscriber = subscriberOpt.get();
//        Map<String, Object> profile = new HashMap<>();
//        profile.put("name", subscriber.getName());
//        profile.put("phoneNumber", subscriber.getPhoneNumber());
//        profile.put("email", subscriber.getEmail());
//        profile.put("alternateMobile", subscriber.getAlternateMobile() != null ? subscriber.getAlternateMobile() : "");
//        profile.put("address", subscriber.getAddress() != null ? subscriber.getAddress() : "");
//        profile.put("dob", subscriber.getDob() != null ? subscriber.getDob().toString() : "");
//        return ResponseEntity.ok(profile);
//    }
//
//    @PutMapping("/profile")
//    public ResponseEntity<?> updateProfile(@RequestBody SubscriberUpdateRequest updateRequest, Principal principal) {
//        if (principal == null || principal.getName() == null) {
//            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
//        }
//
//        String phoneNumber = principal.getName();
//        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
//        if (subscriberOpt.isEmpty()) {
//            return ResponseEntity.status(404).body(new ResponseMessage("Not Found", "Subscriber not found"));
//        }
//
//        Subscriber subscriber = subscriberOpt.get();
//
//        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
//            if (!updateRequest.getEmail().equals(subscriber.getEmail()) &&
//                subscriberRepository.findByEmail(updateRequest.getEmail()).isPresent()) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Email already in use"));
//            }
//            subscriber.setEmail(updateRequest.getEmail().trim());
//        }
//        if (updateRequest.getAlternateMobile() != null) {
//            if (!updateRequest.getAlternateMobile().isEmpty() && 
//                !updateRequest.getAlternateMobile().matches("^[0-9]{10}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid alternate mobile number"));
//            }
//            subscriber.setAlternateMobile(updateRequest.getAlternateMobile().trim());
//        }
//        if (updateRequest.getAddress() != null) {
//            subscriber.setAddress(updateRequest.getAddress().trim());
//        }
//
//        subscriberRepository.save(subscriber);
//        return ResponseEntity.ok(new ResponseMessage("Success", "Profile updated successfully"));
//    }
//
//    @PostMapping("/recharge")
//    public ResponseEntity<?> purchasePlan(@RequestBody PaymentRequestDTO paymentDTO, Principal principal) {
//        System.out.println("Received paymentDTO: " + paymentDTO);
//
//        if (paymentDTO == null) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Payment details are missing"));
//        }
//
//        if (principal == null || principal.getName() == null) {
//            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
//        }
//
//        String phoneNumber = principal.getName();
//
//        // Validate paymentDTO fields
//        if (paymentDTO.getMobileNumber() == null || !paymentDTO.getMobileNumber().matches("^[0-9]{10}$")) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid mobile number"));
//        }
//
//        if (paymentDTO.getPlanId() == null) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Plan ID is required"));
//        }
//
//        if (paymentDTO.getPaymentMethod() == null || paymentDTO.getPaymentMethod().isEmpty()) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Payment method is required"));
//        }
//
//        if (paymentDTO.getAmount() <= 0) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid amount"));
//        }
//
//        // Validate payment method-specific fields
//        if ("upi".equals(paymentDTO.getPaymentMethod())) {
//            String upiId = paymentDTO.getUpiId();
//            if (upiId == null || upiId.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "UPI ID is required"));
//            }
//            if (!upiId.matches("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid UPI ID format. Must be 2-256 characters before @ and 2-64 letters after @"));
//            }
//        } else if ("bank-transfer".equals(paymentDTO.getPaymentMethod())) {
//            if (paymentDTO.getAccountHolder() == null || paymentDTO.getAccountHolder().isEmpty()) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Account holder name is required"));
//            }
//            if (paymentDTO.getBankAccount() == null || !paymentDTO.getBankAccount().matches("^\\d{9,18}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid bank account number"));
//            }
//            if (paymentDTO.getIfscCode() == null || !paymentDTO.getIfscCode().matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid IFSC code"));
//            }
//            if (paymentDTO.getBankName() == null || paymentDTO.getBankName().isEmpty()) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Bank name is required"));
//            }
//        } else if ("card-fields".equals(paymentDTO.getPaymentMethod())) {
//            if (paymentDTO.getCardHolderName() == null || paymentDTO.getCardHolderName().isEmpty()) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Card holder name is required"));
//            }
//            if (paymentDTO.getCardNumber() == null || !paymentDTO.getCardNumber().matches("^\\d{16}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid card number"));
//            }
//            if (paymentDTO.getExpiryDate() == null || !paymentDTO.getExpiryDate().matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid expiry date format (MM/YY)"));
//            }
//            if (paymentDTO.getCvv() == null || !paymentDTO.getCvv().matches("^\\d{3,4}$")) {
//                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid CVV"));
//            }
//        } else {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid payment method"));
//        }
//
//        try {
//            Transaction transaction = subscriberPlanPaymentService.processRecharge(phoneNumber, paymentDTO);
//            TransactionResponseDTO response = subscriberPlanPaymentService.convertToDTO(transaction);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(new ResponseMessage("Internal Server Error", "Unexpected error: " + e.getMessage()));
//        }
//    }
//
//    @GetMapping("/transactions")
//    public ResponseEntity<?> getTransactions(Principal principal,
//                                             @RequestParam(defaultValue = "0") int page,
//                                             @RequestParam(defaultValue = "10") int size,
//                                             @RequestParam(defaultValue = "createdAt") String sortBy,
//                                             @RequestParam(defaultValue = "desc") String sortDir) {
//
//        if (principal == null || principal.getName() == null) {
//            ResponseMessage responseMessage = new ResponseMessage("Unauthorized", "Principal information is missing");
//            return ResponseEntity.status(401).body(responseMessage);
//        }
//
//        String phoneNumber = principal.getName();
//        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//        PageRequest pageable = PageRequest.of(page, size, sort);
//
//        Page<TransactionResponseDTO> transactions = subscriberPlanPaymentService.getTransactions(phoneNumber, pageable);
//        if (transactions.isEmpty()) {
//            ResponseMessage responseMessage = new ResponseMessage("No Content", "No transactions found");
//            return ResponseEntity.ok(responseMessage);
//        }
//
//        return ResponseEntity.ok(transactions);
//    }
//    
//    @PreAuthorize("hasAuthority('SUBSCRIBER')")
//    @GetMapping("/notifications")
//    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long subscriberId) {
//        return ResponseEntity.ok(notificationService.getNotificationsBySubscriberId(subscriberId));
//    }
//
//    @PreAuthorize("hasAuthority('SUBSCRIBER')")
//    @PutMapping("/notifications/{id}/read")
//    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long id) {
//        notificationService.markAsRead(id);
//        return ResponseEntity.ok("Notification marked as read");
//    }
//    
//    //QUICK RECHARGE
//    
//}
//



package com.mobileprepaid.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.mobileprepaid.dto.PaymentRequestDTO;
import com.mobileprepaid.dto.ResponseMessage;
import com.mobileprepaid.dto.SubscriberUpdateRequest;
import com.mobileprepaid.dto.TransactionResponseDTO;
import com.mobileprepaid.entities.Notification;
import com.mobileprepaid.entities.Plan;
import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.entities.Transaction;
import com.mobileprepaid.repository.SubscriberRepository;
import com.mobileprepaid.services.NotificationService;
import com.mobileprepaid.services.PlanService;
import com.mobileprepaid.services.SubscriberPlanPaymentService;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('SUBSCRIBER')")
@RequestMapping("/subscriber")
public class SubscriberController {

    @Autowired
    private PlanService planService;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SubscriberPlanPaymentService subscriberPlanPaymentService;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/plans")
    public ResponseEntity<?> getAllActivePlans(
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

        Page<Plan> plans = planService.getFilteredActivePlans(name, category, dataLimit, validity, pageable);
        if (plans.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
        }

        String phoneNumber = principal.getName();
        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
        if (subscriberOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ResponseMessage("Not Found", "Subscriber not found"));
        }

        Subscriber subscriber = subscriberOpt.get();
        Map<String, Object> profile = new HashMap<>();
        profile.put("name", subscriber.getName());
        profile.put("phoneNumber", subscriber.getPhoneNumber());
        profile.put("email", subscriber.getEmail());
        profile.put("alternateMobile", subscriber.getAlternateMobile() != null ? subscriber.getAlternateMobile() : "");
        profile.put("address", subscriber.getAddress() != null ? subscriber.getAddress() : "");
        profile.put("dob", subscriber.getDob() != null ? subscriber.getDob().toString() : "");
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody SubscriberUpdateRequest updateRequest, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
        }

        String phoneNumber = principal.getName();
        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
        if (subscriberOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new ResponseMessage("Not Found", "Subscriber not found"));
        }

        Subscriber subscriber = subscriberOpt.get();

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            if (!updateRequest.getEmail().equals(subscriber.getEmail()) &&
                subscriberRepository.findByEmail(updateRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Email already in use"));
            }
            subscriber.setEmail(updateRequest.getEmail().trim());
        }
        if (updateRequest.getAlternateMobile() != null) {
            if (!updateRequest.getAlternateMobile().isEmpty() && 
                !updateRequest.getAlternateMobile().matches("^[0-9]{10}$")) {
                return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid alternate mobile number"));
            }
            subscriber.setAlternateMobile(updateRequest.getAlternateMobile().trim());
        }
        if (updateRequest.getAddress() != null) {
            subscriber.setAddress(updateRequest.getAddress().trim());
        }

        subscriberRepository.save(subscriber);
        return ResponseEntity.ok(new ResponseMessage("Success", "Profile updated successfully"));
    }

    @PostMapping("/recharge/initiate")
    public ResponseEntity<?> initiateRecharge(@RequestBody PaymentRequestDTO paymentDTO, Principal principal) {
        if (paymentDTO == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Payment details are missing"));
        }

        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
        }

        String phoneNumber = principal.getName();

        // Validate paymentDTO fields
        if (paymentDTO.getMobileNumber() == null || !paymentDTO.getMobileNumber().matches("^[0-9]{10}$")) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid mobile number"));
        }

        if (paymentDTO.getPlanId() == null) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Plan ID is required"));
        }

        if (paymentDTO.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", "Invalid amount"));
        }

        try {
            Transaction transaction = subscriberPlanPaymentService.initiateRecharge(phoneNumber, paymentDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("transactionId", transaction.getTransactionId());
            response.put("razorpayOrderId", transaction.getRazorpayOrderId());
            response.put("amount", transaction.getAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseMessage("Internal Server Error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/recharge/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpaySignature,
            Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(401).body(new ResponseMessage("Unauthorized", "User not authenticated"));
        }

        String phoneNumber = principal.getName();

        try {
            Transaction transaction = subscriberPlanPaymentService.processRecharge(phoneNumber, razorpayPaymentId, razorpayOrderId, razorpaySignature);
            TransactionResponseDTO response = subscriberPlanPaymentService.convertToDTO(transaction);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ResponseMessage("Internal Server Error", "Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(Principal principal,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "createdAt") String sortBy,
                                             @RequestParam(defaultValue = "desc") String sortDir) {

        if (principal == null || principal.getName() == null) {
            ResponseMessage responseMessage = new ResponseMessage("Unauthorized", "Principal information is missing");
            return ResponseEntity.status(401).body(responseMessage);
        }

        String phoneNumber = principal.getName();
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<TransactionResponseDTO> transactions = subscriberPlanPaymentService.getTransactions(phoneNumber, pageable);
        if (transactions.isEmpty()) {
            ResponseMessage responseMessage = new ResponseMessage("No Content", "No transactions found");
            return ResponseEntity.ok(responseMessage);
        }

        return ResponseEntity.ok(transactions);
    }
    
    @PreAuthorize("hasAuthority('SUBSCRIBER')")
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long subscriberId) {
        return ResponseEntity.ok(notificationService.getNotificationsBySubscriberId(subscriberId));
    }

    @PreAuthorize("hasAuthority('SUBSCRIBER')")
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }
}