package com.example.MovieBooking.dto;

import com.example.MovieBooking.entity.type.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponseDto {
    private Long id;
    private BookingStatus bookingStatus;
    private Timestamp bookingTime;
    private BigDecimal totalAmount;

    // Flattened details from the Show
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime showStartTime;

    // Simple list of seat numbers
    private List<String> bookedSeats;
}