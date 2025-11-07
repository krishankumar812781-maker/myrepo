package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.MovieDto;
import com.example.MovieBooking.entity.Movie;
import com.example.MovieBooking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    private final ModelMapper modelMapper;


    public MovieDto addMovie(MovieDto movieDto) {

        Movie movie=modelMapper.map(movieDto, Movie.class);
        Movie savedMovie= movieRepository.save(movie);
        return modelMapper.map(savedMovie, MovieDto.class);

    }

    public List<MovieDto> getAllMovies() {
        List<Movie> movies= movieRepository.findAll();
        return movies
                .stream()
                .map(Movie -> modelMapper.map(Movie, MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMoviesByGenre(String genre) {
        Optional<List<Movie>> listOfMovies= movieRepository.findByGenre(genre);
        if(listOfMovies.isEmpty()){
            throw new RuntimeException("No movies found for the genre: "+genre);
        }
        return listOfMovies.get()
                .stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMoviesByLanguage(String language) {
        Optional<List<Movie>> listOfMovies= movieRepository.findByLanguage(language);
        if(listOfMovies.isEmpty()){
            throw new RuntimeException("No movies found for the language: "+language);
        }
        return listOfMovies.get()
                .stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .toList();
    }

    public MovieDto getMoviesByTitle(String title) {
        Optional<Movie> movie= movieRepository.findByTitle(title);
        if(movie.isEmpty()){
            throw new RuntimeException("No movies found with the title: "+title);
        }
        return modelMapper.map( movie.get(), MovieDto.class);
    }

    public MovieDto updateMovie(Long id, MovieDto movieDto) {
        Movie movie=movieRepository.findById(id).orElseThrow(()->new RuntimeException("Movie not found with id: "+id));
        movie.setTitle(movieDto.getTitle());
        movie.setGenre(movieDto.getGenre());
        movie.setLanguage(movieDto.getLanguage());
        movie.setDuration(movieDto.getDuration());
        movie.setDescription(movieDto.getDescription());
        movie.setPosterUrl(movieDto.getPosterUrl());

       Movie mov= movieRepository.save(movie);
         return modelMapper.map(mov, MovieDto.class);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}
