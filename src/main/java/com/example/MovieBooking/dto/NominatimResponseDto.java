package com.example.MovieBooking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// This tells Jackson to ignore all the other fields in the
// JSON response that we don't care about.
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NominatimResponseDto {

    // The JSON field is "lat", we map it to our "latitude" variable
    @JsonProperty("lat")
    private String latitude;

    // The JSON field is "lon", we map it to our "longitude" variable
    @JsonProperty("lon")
    private String longitude;
}