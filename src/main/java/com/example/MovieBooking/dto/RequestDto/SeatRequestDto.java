package com.example.MovieBooking.dto.RequestDto;

import lombok.Data;
import java.util.List;

@Data
public class SeatRequestDto {
    private Long screenId;
    private List<SeatInfo> seats;

    @Data
    public static class SeatInfo {
        private String seatNumber; // "A1"
        private String seatType;   // "REGULAR"
    }
}