package com.example.MovieBooking.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private RestTemplate restTemplate; // Use RestTemplate for API calls

    @Value("${MAILTRAP_API_TOKEN}")
    private String apiToken;

    @Value("${MAILTRAP_INBOX_ID}")
    private String inboxId;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Internal helper method to send HTML emails via Mailtrap Sandbox API (Port 443)
     */
    private void sendHtmlEmail(String to, String subject, String title, String bodyContent) {
        // FIXED URL: Using sandbox domain and path
        String url = "https://sandbox.api.mailtrap.io/api/send/" + inboxId;

        try {
            // Professional HTML Template
            String htmlContent =
                    "<div style='font-family: Helvetica, Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                            "<div style='background-color: #e50914; padding: 20px; text-align: center;'>" +
                            "<h1 style='color: white; margin: 0;'>ShowTime Tix</h1>" +
                            "</div>" +
                            "<div style='padding: 30px; color: #333; line-height: 1.6;'>" +
                            "<h2 style='color: #444;'>" + title + "</h2>" +
                            "<p>" + bodyContent + "</p>" +
                            "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                            "<p style='font-size: 12px; color: #777;'>This is an automated notification from ShowTime Tix. Please do not reply to this email.</p>" +
                            "</div>" +
                            "</div>";

            // Prepare Request Payload for Mailtrap API
            MailtrapRequest request = new MailtrapRequest(
                    new MailtrapRequest.User(fromEmail, "ShowTime Tix"),
                    List.of(new MailtrapRequest.User(to, "Valued User")),
                    subject,
                    htmlContent
            );

            // Set Headers - Mailtrap Sandbox API uses 'Api-Token'
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Api-Token", apiToken);

            HttpEntity<MailtrapRequest> entity = new HttpEntity<>(request, headers);

            // Send POST request via HTTPS
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                LOGGER.info("SUCCESS: Email captured in Mailtrap Sandbox (Inbox: {})", inboxId);
            }
        } catch (Exception e) {
            LOGGER.error("MAILTRAP API ERROR for {}: {}", to, e.getMessage());
        }
    }

    @KafkaListener(topics = "booking-confirmed-topic", groupId = "notification-group")
    public void handleBookingConfirmation(String message) {
        LOGGER.info("Received booking confirmation event for: {}", message);
        sendHtmlEmail(message,
                "Booking Confirmed! - ShowTime Tix",
                "Your Tickets are Ready!",
                "Grab the popcorn! Your booking has been confirmed. You can view your digital tickets in the 'My Bookings' section of the app.");
    }

    @KafkaListener(topics = "booking-cancelled-topic", groupId = "notification-group")
    public void handleBookingCancellation(String message) {
        LOGGER.info("Received booking cancellation event for: {}", message);
        sendHtmlEmail(message,
                "Booking Cancelled",
                "Refund Processed",
                "Your booking has been successfully cancelled. The refund will be credited to your original payment method within 5-7 business days.");
    }

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-group")
    public void handleUserRegistration(String message) {
        LOGGER.info("Sending welcome email to: {}", message);
        sendHtmlEmail(message,
                "Welcome to ShowTime Tix!",
                "Ready for the Big Screen?",
                "Thank you for joining ShowTime Tix! We are excited to have you. You can now browse the latest blockbusters and book your favorite seats in seconds.");
    }

    @KafkaListener(topics = "show-updated-topic", groupId = "notification-group")
    public void handleShowUpdate(String message) {
        LOGGER.info("Processing show update notification: {}", message);
    }

    @PostConstruct
    public void initTest() {
        LOGGER.info("Sending startup API test email to Mailtrap Sandbox...");
        String myRealEmail = "kkydv4546@gmail.com";

        sendHtmlEmail(myRealEmail,
                "Mailtrap API Test",
                "Connection Success",
                "This email confirms your Mailtrap API configuration is working and bypassing network blocks.");
    }

    // --- Helper DTOs for Mailtrap API JSON ---
    @Data @AllArgsConstructor
    static class MailtrapRequest {
        private User from;
        private List<User> to;
        private String subject;
        private String html;

        @Data @AllArgsConstructor static class User {
            private String email;
            private String name;
        }
    }
}