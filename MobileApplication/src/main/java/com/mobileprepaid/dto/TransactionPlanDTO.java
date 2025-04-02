package com.mobileprepaid.dto;

import lombok.Data;

@Data
public class TransactionPlanDTO {
    private String name;
    private Integer validity;
    private String dataLimit;
    private String callLimit;
    private String smsLimit;
    private double price;
}