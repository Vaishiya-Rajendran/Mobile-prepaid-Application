package com.mobileprepaid.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin") // More standard naming
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password") // Avoid exposing passwords
public class AdminLogin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password; // Ensure it is hashed before storing

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
