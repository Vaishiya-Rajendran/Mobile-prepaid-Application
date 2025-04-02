package com.mobileprepaid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.enums.SubscriberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    
    Optional<Subscriber> findByEmail(String email);
    
    Optional<Subscriber> findByPhoneNumber(String phoneNumber);
    
    Optional<Subscriber> findById(Long id);
    long countByStatus(SubscriberStatus status);
    // ✅ Improved search query to handle spaces and case insensitivity
    @Query("SELECT s FROM Subscriber s WHERE " +
            "(:search IS NULL OR TRIM(LOWER(s.name)) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR TRIM(LOWER(s.email)) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR TRIM(LOWER(s.phoneNumber)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Subscriber> searchSubscribers(@Param("search") String search, Pageable pageable);

    // ✅ Fetch subscribers by status (if status is null, fetch all)
    Page<Subscriber> findByStatus(@Param("status") SubscriberStatus status, Pageable pageable);
}
