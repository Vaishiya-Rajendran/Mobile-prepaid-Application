package com.mobileprepaid.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.enums.SubscriberStatus;
import com.mobileprepaid.repository.SubscriberRepository;

@Service
public class AdminService {
    private final SubscriberRepository subscriberRepository;

    public AdminService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    // ✅ Fetch subscribers with pagination & filtering by status
    public Page<Subscriber> getSubscribersByStatus(SubscriberStatus status, Pageable pageable) {
        return subscriberRepository.findByStatus(status, pageable);
    }
}
