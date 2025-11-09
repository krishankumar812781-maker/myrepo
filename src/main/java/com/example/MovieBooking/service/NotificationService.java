package com.example.MovieBooking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);




    /**
     * Listens to the "booking-confirmed-topic".
     * This method will automatically be triggered when a message is sent.
     */
    @KafkaListener(topics = "booking-confirmed-topic", groupId = "notification-group")
    public void handleBookingConfirmation(String message) {
        LOGGER.info("Received booking confirmation: {}", message);

        // In a real application, you would add your
        // email-sending logic here (e.g., using JavaMailSender).
    }

    @KafkaListener(topics = "booking-cancelled-topic", groupId = "notification-group")
    public void handleBookingCancellation(String message) {
        LOGGER.info("Received booking cancellation: {}", message);
        // Add logic to send a "Booking Cancelled" email
    }

    @KafkaListener(topics = "user-registered-topic", groupId = "notification-group")
    public void handleUserRegistration(String message) {
        // The message is the user's email
        LOGGER.info("Sending welcome email to: {}", message);
        // Add email sending logic for "Welcome!"
    }

    @KafkaListener(topics = "show-updated-topic", groupId = "notification-group")
    public void handleShowUpdate(String message) {
        // The message is a JSON string: {"showId": 1, "newStartTime": "..."}
        LOGGER.info("Notifying users of show update: {}", message);
        // Add logic to find all users for this showId and email them.
    }
}