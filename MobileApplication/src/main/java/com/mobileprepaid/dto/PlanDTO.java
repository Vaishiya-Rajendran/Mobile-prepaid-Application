package com.mobileprepaid.dto;

import lombok.Data;

@Data
public class PlanDTO {
    private Long categoryId;
    private String name;
    private double price;
    private String categoryName;
    private String validity;
    private String dataLimit;
    private String smsLimit;
    private String callLimit;
    private String status;
}