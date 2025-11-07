package com.example.MovieBooking.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {


    private String title;

    private String description;


    private String language;


    private String genre;


    private Integer duration; // Duration in minutes

    private String posterUrl;
}
