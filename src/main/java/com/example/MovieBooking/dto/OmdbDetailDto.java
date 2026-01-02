package com.example.MovieBooking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbDetailDto {
    private Long id;
    @JsonProperty("Title") private String title;
    @JsonProperty("Plot") private String plot;
    @JsonProperty("Genre") private String genre;
    @JsonProperty("Language") private String language;
    @JsonProperty("Runtime") private String runtime;
    @JsonProperty("Poster") private String poster;
}
