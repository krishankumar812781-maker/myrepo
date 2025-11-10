package com.example.MovieBooking.dto;

import lombok.Data;

@Data
public class TheaterResponseDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private Double latitude;  // The field from the API
    private Double longitude; // The field from the API
}