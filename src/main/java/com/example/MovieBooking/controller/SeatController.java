package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.SeatRequestDto;
import com.example.MovieBooking.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addSeats(@RequestBody SeatRequestDto requestDto) {
        return ResponseEntity.ok(seatService.addSeats(requestDto));
    }
}