package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.ShowResponseDto;
import com.example.MovieBooking.dto.RequestDto.TheaterDto;
import com.example.MovieBooking.service.ShowService;
import com.example.MovieBooking.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;

    @Autowired
    private ShowService showService;

    // --- Public Endpoints (for any user) ---

    @GetMapping
    public ResponseEntity<List<TheaterDto>> getAllTheaters() {
        List<TheaterDto> theaters = theaterService.getAllTheaters();
        return ResponseEntity.ok(theaters);
    }
    @GetMapping("/city")
    public ResponseEntity<List<TheaterDto>> getTheaterByLocation(@RequestParam String city) {
        List<TheaterDto> theaters = theaterService.findTheatersByCity(city);
        return ResponseEntity.ok(theaters);
    }

    @GetMapping("/{theaterId}/shows")
    public ResponseEntity<List<ShowResponseDto>> getShowsForTheater(@PathVariable Long theaterId) {
        List<ShowResponseDto> shows = showService.getShowsByTheaterId(theaterId); //its inside show service
        return ResponseEntity.ok(shows);
    }

    // --- Admin Endpoints (Secured) ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterDto> addTheater(@RequestBody TheaterDto theaterDTO) {
        TheaterDto newTheater = theaterService.addTheater(theaterDTO);
        return new ResponseEntity<>(newTheater, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TheaterDto> updateTheater(@PathVariable Long id, @RequestBody TheaterDto theaterDTO) {
        TheaterDto updatedTheater = theaterService.updateTheater(id, theaterDTO);
        return ResponseEntity.ok(updatedTheater);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.ok("Theater deleted successfully");
    }
}
