package com.mobileprepaid.services;

import com.mobileprepaid.entities.AdminLogin;
import com.mobileprepaid.entities.SubscriberLogin;
import com.mobileprepaid.repository.AdminLoginRepository;
import com.mobileprepaid.repository.SubscriberLoginRepository;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AdminLoginRepository adminLoginRepository;
    private final SubscriberLoginRepository subscriberLoginRepository;

    public UserDetailsServiceImpl(AdminLoginRepository adminLoginRepository, SubscriberLoginRepository subscriberLoginRepository) {
        this.adminLoginRepository = adminLoginRepository;
        this.subscriberLoginRepository = subscriberLoginRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 🔹 Check if user is an Admin
        AdminLogin admin = adminLoginRepository.findByEmail(username).orElse(null);
        if (admin != null) {
            return User.withUsername(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getRole().toString())))
                    .build();
        }

        // 🔹 Check if user is a Subscriber
        SubscriberLogin subscriberLogin = subscriberLoginRepository.findByPhoneNumber(username).orElse(null);
        if (subscriberLogin != null) {
            return User.withUsername(subscriberLogin.getPhoneNumber())
                    .password("") // No password needed for OTP authentication
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + subscriberLogin.getSubscriber().getRole().toString())))
                    .build();
        }

        // 🔹 If user is not found
        throw new UsernameNotFoundException("User not found with email/phone: " + username);
    }
}
