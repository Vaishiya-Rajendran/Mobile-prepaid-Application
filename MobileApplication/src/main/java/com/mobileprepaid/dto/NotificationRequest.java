package com.mobileprepaid.dto;

public class NotificationRequest {
    private String name;
    private String mobileNumber;
    private String message;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; } // Fixed typo
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}