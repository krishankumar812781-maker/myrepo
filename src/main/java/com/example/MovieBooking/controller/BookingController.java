package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.BookingResponseDto;
import com.example.MovieBooking.dto.RequestDto.BookingRequestDto;
import com.example.MovieBooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/createbooking")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingRequestDto bookingRequestDto, Principal principal){
        String userEmail = principal.getName();
        BookingResponseDto newBooking = bookingService.createBooking(userEmail, bookingRequestDto);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }


    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(Principal principal) {
        return ResponseEntity.ok(bookingService.getBookingsForUser(principal.getName()));
    }

    /**
     * Gets a single booking by ID, if it belongs to the user.
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BookingResponseDto> getMyBookingById(
            @PathVariable Long bookingId, Principal principal) {

        return ResponseEntity.ok(bookingService.getBookingById(bookingId, principal.getName()));
    }

    /**
     * Cancels a booking by ID, if it belongs to the user.
     */
    @PostMapping("/{bookingId}/cancel") // Using POST for a state-changing operation
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable Long bookingId, Principal principal) {

        BookingResponseDto cancelledBooking = bookingService.cancelBooking(bookingId, principal.getName());
        return ResponseEntity.ok(cancelledBooking);
    }


}
