package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.SeatRequestDto;
import com.example.MovieBooking.entity.Screen;
import com.example.MovieBooking.entity.Seat;
import com.example.MovieBooking.repository.ScreenRepository;
import com.example.MovieBooking.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Transactional
    public String addSeats(SeatRequestDto requestDto) {
        Screen screen = screenRepository.findById(requestDto.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));

        List<Seat> seatsToSave = requestDto.getSeats().stream()
                .map(seatInfo -> {
                    Seat seat = new Seat();
                    seat.setScreen(screen);
                    seat.setSeatNumber(seatInfo.getSeatNumber());
                    seat.setSeatType(seatInfo.getSeatType());
                    return seat;
                })
                .toList();

        seatRepository.saveAll(seatsToSave);
        return "Added " + seatsToSave.size() + " seats to screen " + screen.getName();
    }
}