package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.MovieDto;
import com.example.MovieBooking.dto.OmdbDetailDto;
import com.example.MovieBooking.dto.OmdbSearchResponse;
import com.example.MovieBooking.dto.OmdbSearchResult;
import com.example.MovieBooking.entity.Movie;
import com.example.MovieBooking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Value("${omdb.api.key}")
    private String apiKey;

    public List<OmdbSearchResult> searchOmdb(String title) {
        String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&s=" + title;
        OmdbSearchResponse response = restTemplate.getForObject(url, OmdbSearchResponse.class);
        return (response != null && response.getSearchResults() != null)
                ? response.getSearchResults() : List.of();
    }

    public MovieDto importMovieByImdbId(String imdbId) {
        String url = "http://www.omdbapi.com/?apikey=" + apiKey + "&i=" + imdbId + "&plot=full";
        OmdbDetailDto detail = restTemplate.getForObject(url, OmdbDetailDto.class);

        if (detail == null || detail.getTitle() == null) {
            throw new RuntimeException("Movie not found on OMDb for ID: " + imdbId);
        }

        Movie movie = new Movie();
        // ⚡ FIXED: Removed movie.setId(detail.getId()).
        // Database will now auto-generate the numeric ID.

        movie.setTitle(detail.getTitle());
        movie.setDescription(detail.getPlot());
        movie.setGenre(detail.getGenre());
        movie.setLanguage(detail.getLanguage());
        movie.setPosterUrl(detail.getPoster());

        try {
            String runtimeStr = detail.getRuntime().split(" ")[0];
            movie.setDuration(Integer.parseInt(runtimeStr));
        } catch (Exception e) {
            movie.setDuration(0);
        }

        Movie savedMovie = movieRepository.save(movie);
        // modelMapper will now include the generated ID in the returned DTO.
        return modelMapper.map(savedMovie, MovieDto.class);
    }

    public MovieDto addMovie(MovieDto movieDto) {
        Movie movie = modelMapper.map(movieDto, Movie.class);
        Movie savedMovie = movieRepository.save(movie);
        return modelMapper.map(savedMovie, MovieDto.class);
    }

    public List<MovieDto> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream().map(m -> modelMapper.map(m, MovieDto.class)).toList();
    }

    public List<MovieDto> getMoviesByGenre(String genre) {
        Optional<List<Movie>> listOfMovies = movieRepository.findByGenre(genre);
        if (listOfMovies.isEmpty()) throw new RuntimeException("No movies found for genre: " + genre);
        return listOfMovies.get().stream().map(m -> modelMapper.map(m, MovieDto.class)).toList();
    }

    public List<MovieDto> getMoviesByLanguage(String language) {
        Optional<List<Movie>> listOfMovies = movieRepository.findByLanguage(language);
        if (listOfMovies.isEmpty()) throw new RuntimeException("No movies found for language: " + language);
        return listOfMovies.get().stream().map(m -> modelMapper.map(m, MovieDto.class)).toList();
    }

    public MovieDto getMoviesByTitle(String title) {
        Optional<Movie> movie = movieRepository.findByTitle(title);
        if (movie.isEmpty()) throw new RuntimeException("No movies found with title: " + title);
        return modelMapper.map(movie.get(), MovieDto.class);
    }

    public MovieDto updateMovie(Long id, MovieDto movieDto) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        modelMapper.map(movieDto, movie);
        Movie updated = movieRepository.save(movie);
        return modelMapper.map(updated, MovieDto.class);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
    }
}