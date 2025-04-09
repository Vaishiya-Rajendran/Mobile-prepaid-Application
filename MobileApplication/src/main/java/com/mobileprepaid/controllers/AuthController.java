package com.mobileprepaid.controllers;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.mobileprepaid.dto.AdminRegisterRequest;
import com.mobileprepaid.dto.SubscriberRegisterRequest;
import com.mobileprepaid.dto.LoginRequest;
import com.mobileprepaid.dto.OtpRequest;
import com.mobileprepaid.services.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    
    @PostMapping("/register/subscriber")
    public ResponseEntity<Map<String, String>> registerSubscriber(@RequestBody SubscriberRegisterRequest request) {
        try {
            String message = authService.registerSubscriber(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", "Registration failed: " + ex.getMessage()));
        }
    }

    // Admin Login
    @PostMapping("/login/admin")
    public ResponseEntity<Map<String, String>> loginAdmin(@RequestBody LoginRequest request) {
        String token = authService.loginAdmin(request);
        return ResponseEntity.ok(Map.of("token", token));
    }


 // Send OTP for Subscriber Login
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody OtpRequest request) {
        try {
            // Call the sendOtp method and get the success message
            String message = authService.sendOtp(request);
            return ResponseEntity.ok(Map.of("message", message)); 
        } catch (ResponseStatusException ex) {
            // Handle specific error cases with custom messages and status codes
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        }
    }

    // Verify OTP for Subscriber Login
    @PostMapping("/login/subscriber")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpRequest request) {
        try {
            // Call the verifyOtp method and get the JWT token
            String token = authService.verifyOtp(request);
            return ResponseEntity.ok(Map.of("token", token)); // ✅ Return JSON with token
        } catch (ResponseStatusException ex) {
            // Handle specific error cases with custom messages and status codes
            return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
        }


    }}

