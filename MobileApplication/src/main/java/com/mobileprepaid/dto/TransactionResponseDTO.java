package com.mobileprepaid.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDTO {
    private String transactionId;
    private String username;
    private String phoneNumber;
    private TransactionPlanDTO plan;
    private double amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;
}