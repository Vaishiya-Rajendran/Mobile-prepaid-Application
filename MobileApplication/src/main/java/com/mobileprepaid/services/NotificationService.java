package com.mobileprepaid.services;

import com.mobileprepaid.dto.NotificationRequest;
import com.mobileprepaid.entities.Notification;
import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.entities.Transaction;
import com.mobileprepaid.repository.NotificationRepository;
import com.mobileprepaid.repository.SubscriberRepository;
import com.mobileprepaid.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JavaMailSender mailSender;

    public Notification sendNotification(String name, String mobileNumber, String message) {
        // Fetch subscriber
        Subscriber subscriber = subscriberRepository.findByPhoneNumber(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("Subscriber not found with phone number: " + mobileNumber));

        // Fetch latest transaction
        Transaction latestTransaction = transactionRepository.findTopByPhoneNumberOrderByCreatedAtDesc(mobileNumber)
                .orElseThrow(() -> new IllegalArgumentException("No transaction found for subscriber: " + mobileNumber));

        // Calculate expiry date
        LocalDateTime expiryDate = latestTransaction.getCreatedAt().plusDays(latestTransaction.getPlan().getValidity());

        // Create notification
        Notification notification = new Notification();
        notification.setSubscriberId(subscriber.getId());
        notification.setReceiverId(subscriber.getId());
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setSenderId(1L); // Assuming admin ID is 1; adjust as needed
        notification.setMobileNumber(subscriber.getPhoneNumber());
        notification.setSentAt(LocalDateTime.now()); // Initially set, updated after email
        notification.setUsername(subscriber.getName()); // Assuming username is name; adjust if different
        notification.setName(subscriber.getName());
        notificationRepository.save(notification);

        // Send email
        try {
            sendEmail(subscriber.getEmail(), subscriber.getName(), message, latestTransaction, expiryDate);
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now()); // Update sent_at after successful send
            notificationRepository.save(notification);
        } catch (RuntimeException e) {
            notification.setStatus("FAILED");
            notificationRepository.save(notification);
            throw e;
        }

        return notification;
    }

    private void sendEmail(String toEmail, String subscriberName, String message, Transaction transaction, LocalDateTime expiryDate) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("vaishiyarajendran2404@gmail.com", "Mobi.Comm");
            helper.setTo(toEmail);
            helper.setSubject("Reminder: Your Mobi.Comm Plan is Expiring Soon");

            String emailBody = "<h3>Hello " + subscriberName + ",</h3>" +
                    "<p>" + message + "</p>" +
                    "<h4>Your Plan Details:</h4>" +
                    "<ul>" +
                    "<li><strong>Plan Name:</strong> " + transaction.getPlan().getName() + "</li>" +
                    "<li><strong>Data Limit:</strong> " + transaction.getPlan().getDataLimit() + "</li>" +
                    "<li><strong>Validity:</strong> " + transaction.getPlan().getValidity() + " days</li>" +
                    "<li><strong>Price:</strong> Rs." + transaction.getPlan().getPrice() + "</li>" +
                    "<li><strong>Expiry Date:</strong> " + expiryDate.toLocalDate().toString() + "</li>" +
                    "</ul>" +
                    "<p>Please recharge your plan to continue enjoying our services.</p>" +
                    "<p>Regards,<br>The Mobi.Comm Team</p>";

            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode sender name: " + e.getMessage(), e);
        }
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public List<Notification> getNotificationsBySubscriberId(Long subscriberId) {
        return notificationRepository.findBySubscriberId(subscriberId);
    }

    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public List<Notification> sendNotificationToAll(String message) {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        return subscribers.stream()
                .map(subscriber -> sendNotification(subscriber.getName(), subscriber.getPhoneNumber(), message))
                .toList();
    }
}