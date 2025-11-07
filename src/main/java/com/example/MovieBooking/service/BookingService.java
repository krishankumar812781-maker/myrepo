package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.BookingResponseDto;
import com.example.MovieBooking.dto.RequestDto.BookingRequestDto;
import com.example.MovieBooking.entity.Booking;
import com.example.MovieBooking.entity.Show;
import com.example.MovieBooking.entity.ShowSeat;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.BookingStatus;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.BookingRepository;
import com.example.MovieBooking.repository.ShowRepository;
import com.example.MovieBooking.repository.ShowSeatRepository;
import com.example.MovieBooking.repository.UserRepository;
// 1. REMOVE THE WRONG IMPORT
// import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
// 2. ADD THE CORRECT SPRING IMPORT
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final UserRepository userRepository;
    private final ShowSeatRepository showSeatRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;


    // 3. ADD THE MISSING ANNOTATION
    @Transactional()
    public BookingResponseDto createBooking(String userEmail, BookingRequestDto bookingRequestDto) {
        // 1. Find the User and Show
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        Show show = showRepository.findById(bookingRequestDto.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        // 2. Lock, Validate, and Collect Seats
        List<ShowSeat> lockedSeats = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long showSeatId : bookingRequestDto.getShowSeatIds()) {

            // --- THIS IS THE CRITICAL SECTION ---
            ShowSeat seat = showSeatRepository.findByIdAndLock(showSeatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + showSeatId));

            // --- VALIDATION ---
            // A. Check if seat is for the correct show
            if (!seat.getShow().getId().equals(show.getId())) {
                throw new RuntimeException("Seat " + seat.getSeat().getSeatNumber() + "is not for this show.");
            }

            // B. Check if seat is AVAILABLE (using the enum)
            if (seat.getStatus() != SeatStatus.AVAILABLE) { // <-- 4. USE THE ENUM
                throw new RuntimeException("Seat " + seat.getSeat().getSeatNumber() + " is already booked or locked.");
            }

            // If all checks pass, add to our list and sum the price
            lockedSeats.add(seat);
            totalAmount = totalAmount.add(seat.getPrice());
        }

        // 3. Create the Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(Timestamp.from(Instant.now()));
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        // 4. Update Seats and Link to Booking
        for (ShowSeat seat : lockedSeats) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBooking(savedBooking);
        }

        showSeatRepository.saveAll(lockedSeats);

        // 5. Publish to Kafka
        //kafkaTemplate.send("booking-confirmed-topic", "Booking " + savedBooking.getId() + " confirmed.");

        // 6. Map to a Response DTO and return
        return mapToBookingResponseDto(savedBooking, lockedSeats);
    }


    @Transactional(readOnly = true) // <-- This annotation is correct (from Spring)
    public List<BookingResponseDto> getBookingsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserId(user.getId());

        return bookings.stream()
                .map(booking -> {
                    List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());
                    return mapToBookingResponseDto(booking, seats);
                })
                .toList();
    }


    @Transactional(readOnly = true) // <-- This annotation is correct (from Spring)
    public BookingResponseDto getBookingById(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to this user"));

        List<ShowSeat> seats = showSeatRepository.findByBookingId(booking.getId());

        return mapToBookingResponseDto(booking, seats);
    }

    @Transactional // <-- This annotation is correct (from Spring)
    public BookingResponseDto cancelBooking(Long bookingId, String userEmail) {
        // 1. Find the User and the Booking
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new RuntimeException("Booking not found or does not belong to this user"));

        // 2. Check if it can be cancelled
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled.");
        }

        // 3. Find all ShowSeats for this booking
        List<ShowSeat> seatsToRelease = showSeatRepository.findByBookingId(booking.getId());

        // 4. Update all seats
        for (ShowSeat seat : seatsToRelease) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBooking(null);
        }

        showSeatRepository.saveAll(seatsToRelease);

        // 5. Update the booking itself
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Booking cancelledBooking = bookingRepository.save(booking);

        // 6. Return the updated booking "receipt"
        return mapToBookingResponseDto(cancelledBooking, seatsToRelease);
    }


    private BookingResponseDto mapToBookingResponseDto(Booking booking, List<ShowSeat> seats) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setBookingTime(booking.getBookingTime());
        dto.setTotalAmount(booking.getTotalAmount());

        Show show = booking.getShow();
        dto.setMovieTitle(show.getMovie().getTitle());
        dto.setTheaterName(show.getScreen().getTheater().getName());
        dto.setScreenName(show.getScreen().getName());
        dto.setShowStartTime(show.getStartTime());

        List<String> seatNumbers = seats.stream()
                .map(showSeat -> showSeat.getSeat().getSeatNumber())
                .toList();
        dto.setBookedSeats(seatNumbers);

        return dto;
    }

}