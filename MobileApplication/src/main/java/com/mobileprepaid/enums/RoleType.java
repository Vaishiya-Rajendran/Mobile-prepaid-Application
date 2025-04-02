package com.mobileprepaid.enums;

public enum RoleType {
    ADMIN,      // Represents an administrator with full access
    SUBSCRIBER; // Represents a subscriber with limited access

    @Override
    public String toString() {
        return name().toUpperCase();
    }
}
