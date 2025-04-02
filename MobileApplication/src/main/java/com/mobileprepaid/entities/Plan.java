package com.mobileprepaid.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mobileprepaid.enums.PlanStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int validity;  // Days

    @Column(nullable = false)
    private String dataLimit; // e.g., "2GB/day"

    @Column(nullable = false)
    private String smsLimit; // e.g., "100 SMS/day"

    @Column(nullable = false)
    private String callLimit; // e.g., "Unlimited"

    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    // Custom field to expose category name
    @JsonProperty("categoryName")
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
}
