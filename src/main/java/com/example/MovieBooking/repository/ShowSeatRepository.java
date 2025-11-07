package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat,Long> {


    List<ShowSeat> findByShowId(Long showId);

    boolean existsByShowIdAndStatus(Long showId, String booked);

    /**
     * This is the pessimistic locking method.
     * When called inside a @Transactional method, it will execute
     * a "SELECT ... FOR UPDATE" SQL query, locking the row
     * until the transaction is committed or rolled back.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeat s WHERE s.id = :id")
    Optional<ShowSeat> findByIdAndLock(@Param("id") Long id);

    List<ShowSeat> findByBookingId(Long id);
}
