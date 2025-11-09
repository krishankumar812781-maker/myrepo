package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByUserId(Long id);

    Optional<Booking> findByIdAndUserId(Long bookingId, Long id);

    // This will find all bookings associated with a specific show ID.
    List<Booking> findAllByShowId(Long showId);

}
