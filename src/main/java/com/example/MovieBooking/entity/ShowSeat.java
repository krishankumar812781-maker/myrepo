package com.example.MovieBooking.entity;

import com.example.MovieBooking.entity.type.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "show_seats")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"show", "seat", "booking"}) // Exclude all relationships
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status; // e.g., "AVAILABLE", "LOCKED", "BOOKED"

    @Column(nullable = false)
    private BigDecimal price;

    //relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = true) // Nullable!
    private Booking booking;
}