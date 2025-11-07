package com.example.MovieBooking.repository;

import com.example.MovieBooking.entity.Movie;
import com.example.MovieBooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show,Long> {
    List<Show> findByScreenId(Long screenId);

    List<Show> findShowByMovieId(Long movieId);
}
