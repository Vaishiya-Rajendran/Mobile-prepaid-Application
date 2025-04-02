package com.mobileprepaid.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private String mobileNumber;
    private Long planId;
    private String paymentMethod;
    private double amount;

    // UPI fields
    private String upiId;

    // Bank Transfer fields
    private String accountHolder;
    private String bankAccount;
    private String ifscCode;
    private String bankName;

    // Card fields
    private String cardHolderName;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
}