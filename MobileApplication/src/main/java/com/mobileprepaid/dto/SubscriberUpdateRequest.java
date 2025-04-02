package com.mobileprepaid.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriberUpdateRequest {
    private String email;
    private String alternateMobile;
    private String address;

    // Optional: Default constructor
    public SubscriberUpdateRequest() {}

    // Optional: Parameterized constructor
    public SubscriberUpdateRequest(String email, String alternateMobile, String address) {
        this.email = email;
        this.alternateMobile = alternateMobile;
        this.address = address;
    }
}