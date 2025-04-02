package com.mobileprepaid.services;

import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.entities.Transaction;
import com.mobileprepaid.repository.SubscriberRepository;
import com.mobileprepaid.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TransactionService.class);

    public Transaction initiateQuickRecharge(String phoneNumber) {
        logger.info("Initiating quick recharge for phone number: " + phoneNumber);
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setPhoneNumber(phoneNumber);

        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
        if (subscriberOpt.isPresent()) {
            transaction.setSubscriberId(subscriberOpt.get().getId());
            logger.info("Subscriber found with ID: " + subscriberOpt.get().getId());
        } else {
            logger.info("No subscriber found for phone number: " + phoneNumber);
        }

        transaction.setStatus("PENDING");
        transaction.setPaymentStatus("PENDING");

        logger.info("Saving transaction: " + transaction);
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transaction saved with ID: " + savedTransaction.getId());
        return savedTransaction;
    }
}