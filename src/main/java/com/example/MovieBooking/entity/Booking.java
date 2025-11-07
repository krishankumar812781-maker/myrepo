package com.example.MovieBooking.entity;

import com.example.MovieBooking.entity.type.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "show", "showSeats"}) // Exclude all relationships
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Timestamp bookingTime;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    //Relationships

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)  //there are many bookings for one show
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    
    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL, // When a booking is deleted, its seat links are removed
            fetch = FetchType.LAZY
    )
    private List<ShowSeat> showSeats = new ArrayList<>();
}