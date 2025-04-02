//package com.mobileprepaid.services;
//
//import com.mobileprepaid.dto.PaymentRequestDTO;
//import com.mobileprepaid.dto.TransactionPlanDTO;
//import com.mobileprepaid.dto.TransactionResponseDTO;
//import com.mobileprepaid.entities.Plan;
//import com.mobileprepaid.entities.Subscriber;
//import com.mobileprepaid.entities.Transaction;
//import com.mobileprepaid.repository.PlanRepository;
//import com.mobileprepaid.repository.SubscriberRepository;
//import com.mobileprepaid.repository.TransactionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//public class SubscriberPlanPaymentService {
//
//    @Autowired
//    private PlanRepository planRepository;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private SubscriberRepository subscriberRepository;
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    public Transaction processRecharge(String phoneNumber, PaymentRequestDTO paymentDTO) {
//        try {
//            // Validate paymentDTO
//            if (paymentDTO == null) {
//                throw new IllegalArgumentException("Payment request cannot be null");
//            }
//            if (paymentDTO.getPaymentMethod() == null || paymentDTO.getPaymentMethod().trim().isEmpty()) {
//                throw new IllegalArgumentException("Payment method cannot be null or empty");
//            }
//
//            // Fetch the subscriber
//            Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
//            if (subscriberOpt.isEmpty()) {
//                throw new IllegalArgumentException("Subscriber not found with phone number: " + phoneNumber);
//            }
//            Subscriber subscriber = subscriberOpt.get();
//
//            // Fetch the plan
//            Optional<Plan> planOpt = planRepository.findById(paymentDTO.getPlanId());
//            if (planOpt.isEmpty()) {
//                throw new IllegalArgumentException("Plan not found with ID: " + paymentDTO.getPlanId());
//            }
//            Plan plan = planOpt.get();
//
//            // Validate the amount
//            if (paymentDTO.getAmount() != plan.getPrice()) {
//                throw new IllegalArgumentException("Amount does not match the plan price. Expected: " + plan.getPrice() + ", Provided: " + paymentDTO.getAmount());
//            }
//
//            // Check for an existing pending transaction
//            Optional<Transaction> transactionOpt = transactionRepository.findByPhoneNumberAndStatus(phoneNumber, "PENDING");
//            Transaction transaction;
//
//            if (transactionOpt.isPresent()) {
//                transaction = transactionOpt.get();
//                // Update the existing transaction
//                transaction.setPaymentMethod(paymentDTO.getPaymentMethod());
//                transaction.setSubscriberId(subscriber.getId());
//                transaction.setPlan(plan);
//                transaction.setAmount(paymentDTO.getAmount());
//                transaction.setStatus("SUCCESS");
//                transaction.setPaymentStatus("SUCCESS");
//            } else {
//                // Create a new transaction
//                transaction = new Transaction();
//                transaction.setTransactionId(UUID.randomUUID().toString());
//                transaction.setPhoneNumber(phoneNumber);
//                transaction.setPaymentMethod(paymentDTO.getPaymentMethod());
//                transaction.setSubscriberId(subscriber.getId());
//                transaction.setPlan(plan);
//                transaction.setAmount(paymentDTO.getAmount());
//                transaction.setStatus("SUCCESS");
//                transaction.setPaymentStatus("SUCCESS");
//                transaction.setCreatedAt(LocalDateTime.now());
//            }
//
//            // Save the transaction
//            System.out.println("Saving transaction: " + transaction);
//            transaction = transactionRepository.save(transaction);
//
//            // Send invoice email after successful recharge
//            sendInvoiceEmail(subscriber.getEmail(), subscriber.getName(), transaction, plan);
//
//            return transaction;
//        } catch (ObjectOptimisticLockingFailureException e) {
//            throw new RuntimeException("Failed to process recharge: Concurrent modification detected. Please try again.", e);
//        }
//    }
//
//    public Page<TransactionResponseDTO> getTransactions(String phoneNumber, Pageable pageable) {
//        Page<Transaction> transactions = transactionRepository.findByPhoneNumber(phoneNumber, pageable);
//        return transactions.map(this::convertToDTO);
//    }
//
//    public Page<TransactionResponseDTO> getAllTransactions(Pageable pageable) {
//        try {
//            Page<Transaction> transactions = transactionRepository.findAll(pageable);
//            return transactions.map(this::convertToDTO);
//        } catch (Exception e) {
//            System.out.println("Error fetching all transactions: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Failed to fetch all transactions: " + e.getMessage(), e);
//        }
//    }
//
//    public TransactionResponseDTO convertToDTO(Transaction transaction) {
//        TransactionResponseDTO dto = new TransactionResponseDTO();
//        dto.setTransactionId(transaction.getTransactionId());
//        dto.setPhoneNumber(transaction.getPhoneNumber());
//        dto.setAmount(transaction.getAmount());
//        dto.setPaymentMethod(transaction.getPaymentMethod());
//        dto.setPaymentStatus(transaction.getPaymentStatus());
//        dto.setCreatedAt(transaction.getCreatedAt());
//
//        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(transaction.getPhoneNumber());
//        if (subscriberOpt.isPresent()) {
//            dto.setUsername(subscriberOpt.get().getName());
//        } else {
//            dto.setUsername("Unknown");
//        }
//
//        Plan plan = transaction.getPlan();
//        TransactionPlanDTO planDTO = new TransactionPlanDTO();
//        planDTO.setName(plan.getName());
//        planDTO.setDataLimit(plan.getDataLimit());
//        planDTO.setCallLimit(plan.getCallLimit());
//        planDTO.setSmsLimit(plan.getSmsLimit());
//        planDTO.setValidity(plan.getValidity());
//        planDTO.setPrice(plan.getPrice());
//        dto.setPlan(planDTO);
//
//        return dto;
//    }
//
//    private void sendInvoiceEmail(String toEmail, String subscriberName, Transaction transaction, Plan plan) {
//        MimeMessage mimeMessage = mailSender.createMimeMessage();
//        try {
//            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
//            helper.setFrom("vaishiyarajendran2404@gmail.com", "Mobi.Comm");
//            helper.setTo(toEmail);
//            helper.setSubject("Your Mobi.Comm Recharge Invoice");
//
//            String emailBody = "<h3>Hello " + subscriberName + ",</h3>" +
//                    "<p>Thank you for recharging your mobile number with Mobi.Comm! Below are your invoice details:</p>" +
//                    "<h4>Invoice Details:</h4>" +
//                    "<ul>" +
//                    "<li><strong>Transaction ID:</strong> " + transaction.getTransactionId() + "</li>" +
//                    "<li><strong>Plan Name:</strong> " + plan.getName() + "</li>" +
//                    "<li><strong>Data Limit:</strong> " + plan.getDataLimit() + "</li>" +
//                    "<li><strong>Call Limit:</strong> " + plan.getCallLimit() + "</li>" +
//                    "<li><strong>SMS Limit:</strong> " + plan.getSmsLimit() + "</li>" +
//                    "<li><strong>Validity:</strong> " + plan.getValidity() + " days</li>" +
//                    "<li><strong>Amount:</strong> ₹" + transaction.getAmount() + "</li>" +
//                    "<li><strong>Payment Method:</strong> " + formatPaymentMethod(transaction.getPaymentMethod()) + "</li>" +
//                    "<li><strong>Payment Status:</strong> " + transaction.getPaymentStatus() + "</li>" +
//                    "<li><strong>Mobile Number:</strong> " + transaction.getPhoneNumber() + "</li>" +
//                    "<li><strong>Date & Time:</strong> " + transaction.getCreatedAt().toString() + "</li>" +
//                    "</ul>" +
//                    "<p>We hope you enjoy our services. For any queries, contact us at support@mobicomm.com.</p>" +
//                    "<p>Regards,<br>The Mobi.Comm Team</p>";
//
//            helper.setText(emailBody, true);
//            mailSender.send(mimeMessage);
//            System.out.println("Invoice email sent successfully to " + toEmail);
//        } catch (MessagingException e) {
//            System.err.println("Failed to send invoice email: " + e.getMessage());
//            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
//        } catch (java.io.UnsupportedEncodingException e) {
//            System.err.println("Failed to encode sender name: " + e.getMessage());
//            throw new RuntimeException("Failed to encode sender name: " + e.getMessage(), e);
//        }
//    }
//
//    private String formatPaymentMethod(String method) {
//        return switch (method) {
//            case "upi" -> "UPI Payment";
//            case "card-fields" -> "Card Payment";
//            case "bank-transfer" -> "Bank Transfer";
//            default -> method;
//        };
//    }
//}

package com.mobileprepaid.services;

import com.mobileprepaid.dto.PaymentRequestDTO;
import com.mobileprepaid.dto.TransactionPlanDTO;
import com.mobileprepaid.dto.TransactionResponseDTO;
import com.mobileprepaid.entities.Plan;
import com.mobileprepaid.entities.Subscriber;
import com.mobileprepaid.entities.Transaction;
import com.mobileprepaid.repository.PlanRepository;
import com.mobileprepaid.repository.SubscriberRepository;
import com.mobileprepaid.repository.TransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriberPlanPaymentService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    // Constructor without @Autowired since there are no dependencies to inject
    public SubscriberPlanPaymentService() {
        // Empty constructor
    }

    @PostConstruct
    public void init() {
        try {
            // Initialize RazorpayClient after the bean is constructed and @Value fields are injected
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            // Log the error and throw a runtime exception to fail the application startup
            throw new IllegalStateException("Failed to initialize RazorpayClient: " + e.getMessage(), e);
        }
    }

    public Transaction initiateRecharge(String phoneNumber, PaymentRequestDTO paymentDTO) throws RazorpayException {
        try {
            // Validate paymentDTO
            if (paymentDTO == null) {
                throw new IllegalArgumentException("Payment request cannot be null");
            }

            // Fetch the subscriber
            Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
            if (subscriberOpt.isEmpty()) {
                throw new IllegalArgumentException("Subscriber not found with phone number: " + phoneNumber);
            }
            Subscriber subscriber = subscriberOpt.get();

            // Fetch the plan
            Optional<Plan> planOpt = planRepository.findById(paymentDTO.getPlanId());
            if (planOpt.isEmpty()) {
                throw new IllegalArgumentException("Plan not found with ID: " + paymentDTO.getPlanId());
            }
            Plan plan = planOpt.get();

            // Validate the amount
            if (paymentDTO.getAmount() != plan.getPrice()) {
                throw new IllegalArgumentException("Amount does not match the plan price. Expected: " + plan.getPrice() + ", Provided: " + paymentDTO.getAmount());
            }

            // Check for an existing pending transaction
            Optional<Transaction> transactionOpt = transactionRepository.findByPhoneNumberAndStatus(phoneNumber, "PENDING");
            Transaction transaction;

            if (transactionOpt.isPresent()) {
                transaction = transactionOpt.get();
                // Update the existing transaction
                transaction.setSubscriberId(subscriber.getId());
                transaction.setPlan(plan);
                transaction.setAmount(paymentDTO.getAmount());
            } else {
                // Create a new transaction
                transaction = new Transaction();
                transaction.setTransactionId(UUID.randomUUID().toString());
                transaction.setPhoneNumber(phoneNumber);
                transaction.setSubscriberId(subscriber.getId());
                transaction.setPlan(plan);
                transaction.setAmount(paymentDTO.getAmount());
                transaction.setCreatedAt(LocalDateTime.now());
            }

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (paymentDTO.getAmount() * 100)); // Amount in paise
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", transaction.getTransactionId());

            Order order = razorpayClient.orders.create(orderRequest);
            transaction.setRazorpayOrderId(order.get("id"));

            // Save the transaction with the Razorpay order ID
            transaction = transactionRepository.save(transaction);

            return transaction;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Failed to initiate recharge: Concurrent modification detected. Please try again.", e);
        }
    }

    public Transaction processRecharge(String phoneNumber, String razorpayPaymentId, String razorpayOrderId, String razorpaySignature) throws RazorpayException {
        try {
            // Fetch the transaction by Razorpay order ID
            Optional<Transaction> transactionOpt = transactionRepository.findByRazorpayOrderId(razorpayOrderId);
            if (transactionOpt.isEmpty()) {
                throw new IllegalArgumentException("Transaction not found for Razorpay order ID: " + razorpayOrderId);
            }
            Transaction transaction = transactionOpt.get();

            // Verify the payment signature
            String generatedSignature = generateSignature(razorpayOrderId, razorpayPaymentId, razorpayKeySecret);
            if (!generatedSignature.equals(razorpaySignature)) {
                transaction.setStatus("FAILED");
                transaction.setPaymentStatus("FAILED");
                transactionRepository.save(transaction);
                throw new IllegalArgumentException("Payment signature verification failed");
            }

            // Update transaction with payment details
            transaction.setRazorpayPaymentId(razorpayPaymentId); // Error occurs here
            transaction.setRazorpaySignature(razorpaySignature);
            transaction.setStatus("SUCCESS");
            transaction.setPaymentStatus("SUCCESS");

            // Save the updated transaction
            transaction = transactionRepository.save(transaction);

            // Fetch subscriber for email
            Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(phoneNumber);
            if (subscriberOpt.isEmpty()) {
                throw new IllegalArgumentException("Subscriber not found with phone number: " + phoneNumber);
            }
            Subscriber subscriber = subscriberOpt.get();

            // Send invoice email after successful recharge
            sendInvoiceEmail(subscriber.getEmail(), subscriber.getName(), transaction, transaction.getPlan());

            return transaction;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Failed to process recharge: Concurrent modification detected. Please try again.", e);
        }
    }

    public Page<TransactionResponseDTO> getTransactions(String phoneNumber, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findByPhoneNumber(phoneNumber, pageable);
        return transactions.map(this::convertToDTO);
    }

    public Page<TransactionResponseDTO> getAllTransactions(Pageable pageable) {
        try {
            Page<Transaction> transactions = transactionRepository.findAll(pageable);
            return transactions.map(this::convertToDTO);
        } catch (Exception e) {
            System.out.println("Error fetching all transactions: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch all transactions: " + e.getMessage(), e);
        }
    }

    public TransactionResponseDTO convertToDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setPhoneNumber(transaction.getPhoneNumber());
        dto.setAmount(transaction.getAmount());
        dto.setPaymentMethod(transaction.getPaymentMethod());
        dto.setPaymentStatus(transaction.getPaymentStatus());
        dto.setCreatedAt(transaction.getCreatedAt());

        Optional<Subscriber> subscriberOpt = subscriberRepository.findByPhoneNumber(transaction.getPhoneNumber());
        if (subscriberOpt.isPresent()) {
            dto.setUsername(subscriberOpt.get().getName());
        } else {
            dto.setUsername("Unknown");
        }

        Plan plan = transaction.getPlan();
        TransactionPlanDTO planDTO = new TransactionPlanDTO();
        planDTO.setName(plan.getName());
        planDTO.setDataLimit(plan.getDataLimit());
        planDTO.setCallLimit(plan.getCallLimit());
        planDTO.setSmsLimit(plan.getSmsLimit());
        planDTO.setValidity(plan.getValidity());
        planDTO.setPrice(plan.getPrice());
        dto.setPlan(planDTO);

        return dto;
    }

    private void sendInvoiceEmail(String toEmail, String subscriberName, Transaction transaction, Plan plan) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("vaishiyarajendran2404@gmail.com", "Mobi.Comm");
            helper.setTo(toEmail);
            helper.setSubject("Your Mobi.Comm Recharge Invoice");

            String emailBody = "<h3>Hello " + subscriberName + ",</h3>" +
                    "<p>Thank you for recharging your mobile number with Mobi.Comm! Below are your invoice details:</p>" +
                    "<h4>Invoice Details:</h4>" +
                    "<ul>" +
                    "<li><strong>Transaction ID:</strong> " + transaction.getTransactionId() + "</li>" +
                    "<li><strong>Plan Name:</strong> " + plan.getName() + "</li>" +
                    "<li><strong>Data Limit:</strong> " + plan.getDataLimit() + "</li>" +
                    "<li><strong>Call Limit:</strong> " + plan.getCallLimit() + "</li>" +
                    "<li><strong>SMS Limit:</strong> " + plan.getSmsLimit() + "</li>" +
                    "<li><strong>Validity:</strong> " + plan.getValidity() + " days</li>" +
                    "<li><strong>Amount:</strong> ₹" + transaction.getAmount() + "</li>" +
                    "<li><strong>Payment Method:</strong> Razorpay</li>" +
                    "<li><strong>Payment Status:</strong> " + transaction.getPaymentStatus() + "</li>" +
                    "<li><strong>Mobile Number:</strong> " + transaction.getPhoneNumber() + "</li>" +
                    "<li><strong>Date & Time:</strong> " + transaction.getCreatedAt().toString() + "</li>" +
                    "</ul>" +
                    "<p>We hope you enjoy our services. For any queries, contact us at support@mobicomm.com.</p>" +
                    "<p>Regards,<br>The Mobi.Comm Team</p>";

            helper.setText(emailBody, true);
            mailSender.send(mimeMessage);
            System.out.println("Invoice email sent successfully to " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send invoice email: " + e.getMessage());
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Failed to encode sender name: " + e.getMessage());
            throw new RuntimeException("Failed to encode sender name: " + e.getMessage(), e);
        }
    }

    private String generateSignature(String orderId, String paymentId, String secret) {
        try {
            String payload = orderId + "|" + paymentId;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature: " + e.getMessage(), e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}