package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.ShowRequestDto;
import com.example.MovieBooking.dto.ShowResponseDto;
import com.example.MovieBooking.dto.RequestDto.ShowUpdateRequestDto;
import com.example.MovieBooking.dto.ShowSeatDto;
import com.example.MovieBooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shows") // <-- FIX: Plural and prefixed
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    /**
     * Creates a new show (Admin Only)
     * POST /api/shows
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // <-- FIX: hasAuthority
    public ResponseEntity<ShowResponseDto> createShow(@RequestBody ShowRequestDto showRequestDto){
        return new ResponseEntity<>(showService.createShow(showRequestDto), HttpStatus.CREATED);
    }

    /**
     * Finds all shows. (Public)
     * Can be filtered by:
     * GET /api/shows
     * GET /api/shows?movieId=1
     * GET /api/shows?theaterId=1
     */
    @GetMapping
    public ResponseEntity<List<ShowResponseDto>> getAllShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theaterId
    ){
        if (movieId != null) {
            // FIX: Call the correct service method
            return ResponseEntity.ok(showService.getShowsByMovieId(movieId));
        }
        if (theaterId != null) {
            // FIX: Call the correct service method (we must create this)
            return ResponseEntity.ok(showService.getShowsByTheaterId(theaterId));
        }
        // Default: get all shows
        return new ResponseEntity<>(showService.getAllShows(), HttpStatus.OK);
    }

    /**
     * Gets the seat layout for a specific show. (Public)
     * GET /api/shows/1/seats
     */
    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatDto>> getShowSeatLayout(@PathVariable Long showId) {
        List<ShowSeatDto> seats = showService.getSeatsForShow(showId);
        return new ResponseEntity<>(seats, HttpStatus.OK);
    }

    /**
     * Updates an existing show (Admin Only)
     * PUT /api/shows/1
     */
    @PutMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // <-- FIX: hasAuthority
    public ResponseEntity<ShowResponseDto> updateShow(
            @PathVariable Long showId,
            @RequestBody ShowUpdateRequestDto showUpdateRequestDto){

        return new ResponseEntity<>(showService.updateShow(showId, showUpdateRequestDto), HttpStatus.OK);
    }

    /**
     * Deletes a show (Admin Only)
     * DELETE /api/shows/1
     */
    @DeleteMapping("/{showId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // <-- FIX: hasAuthority
    public ResponseEntity<Void> deleteShow(@PathVariable Long showId){
        showService.deleteShow(showId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}