package com.example.MovieBooking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies", indexes = {
        @Index(name = "idx_movie_language", columnList = "language"),
        @Index(name = "idx_movie_genre", columnList = "genre")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"show"}) // Exclude relationships
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000) // Give more space for description
    private String description;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private Integer duration; // Duration in minutes

    private String posterUrl; // S3 link

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Show> show =new ArrayList<>();
}