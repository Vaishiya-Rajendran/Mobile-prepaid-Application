package com.mobileprepaid.repository;

import com.mobileprepaid.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // ✅ Fetch category by name (case-insensitive)
    Optional<Category> findByNameIgnoreCase(String name);
}
