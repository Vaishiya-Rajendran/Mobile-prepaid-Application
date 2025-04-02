//package com.mobileprepaid.entities;
//
//import lombok.Data;
//import java.time.LocalDateTime;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.PrePersist;
//import jakarta.persistence.PreUpdate;
//import jakarta.persistence.Table;
//import jakarta.persistence.Version;
//
//@Data
//@Entity
//@Table(name = "transactions")
//public class Transaction {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String transactionId;
//
//    @Column(nullable = false)
//    private String phoneNumber;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "plan_id", nullable = false)
//    private Plan plan;
//
//    @Column(nullable = false)
//    private double amount;
//
//    @Column(nullable = false)
//    private String paymentMethod;
//
//    @Column(nullable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    @Column(nullable = false)
//    private String status;
//
//    @Column(name = "subscriber_id", nullable = false)
//    private Long subscriberId;
//
//    @Column(name = "payment_status", nullable = false)
//    private String paymentStatus;
//
//    @Version
//    private Long version;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//        updatedAt = LocalDateTime.now();
//        if (status == null) {
//            status = "SUCCESS";
//        }
//        if (paymentStatus == null) {
//            paymentStatus = "SUCCESS";
//        }
//        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
//            throw new IllegalStateException("Payment method cannot be null or empty before saving transaction");
//        }
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        updatedAt = LocalDateTime.now();
//    }
//}
package com.mobileprepaid.entities;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Data
@Entity
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    // Add Razorpay-specific fields
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING"; // Set default to PENDING until payment is confirmed
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "Razorpay"; // Default to Razorpay
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}