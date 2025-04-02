package com.mobileprepaid.repository;

import com.mobileprepaid.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Existing methods
    Optional<Transaction> findByPhoneNumberAndStatus(String phoneNumber, String status);

    Page<Transaction> findByPhoneNumber(String phoneNumber, Pageable pageable);

    // New method to fetch the latest transaction by phone number, ordered by createdAt descending
    Optional<Transaction> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
}