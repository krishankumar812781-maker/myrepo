package com.example.MovieBooking.dto.RequestDto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDto {
    private String name;

    private String address;

    private String city;

    private Double latitude; // For Google Maps

    private Double longitude; // For Google Maps
}
