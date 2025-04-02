//package com.mobileprepaid.controllers;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.mobileprepaid.dto.QuickRechargeRequestDTO;
//import com.mobileprepaid.dto.QuickRechargeResponseDTO;
//import com.mobileprepaid.entities.Transaction;
//import com.mobileprepaid.services.TransactionService;
//
//@RestController
//@RequestMapping("/public/quick-recharge")
//public class QuickRechargeController {
//
//    private final TransactionService transactionService;
//
//    public QuickRechargeController(TransactionService transactionService) {
//        this.transactionService = transactionService;
//    }
//
//    @PostMapping("/initiate")
//    public ResponseEntity<QuickRechargeResponseDTO> initiateQuickRecharge(@RequestBody QuickRechargeRequestDTO request) {
//        // Validate phone number
//        String phoneNumber = request.getPhoneNumber();
//        if (phoneNumber == null || !phoneNumber.matches("^[6-9]\\d{9}$")) {
//            throw new IllegalArgumentException("Invalid phone number. Please provide a valid 10-digit Indian mobile number.");
//        }
//
//        Transaction transaction = transactionService.initiateQuickRecharge(phoneNumber);
//
//        QuickRechargeResponseDTO response = new QuickRechargeResponseDTO();
//        response.setTransactionId(transaction.getTransactionId());
//        response.setPhoneNumber(transaction.getPhoneNumber());
//
//        return ResponseEntity.ok(response);
//    }
//}