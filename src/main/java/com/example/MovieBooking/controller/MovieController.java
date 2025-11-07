package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.MovieDto;
import com.example.MovieBooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;


    @GetMapping("/getallmovies")
    public ResponseEntity<?> getAllMovies(){

        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("getmoviesbygenre")
    public ResponseEntity<?> getMoviesByGenre(@RequestParam String genre){
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @GetMapping("getmoviesbylanguage")
    public ResponseEntity<?> getMoviesByLanguage(@RequestParam String language){
        return ResponseEntity.ok(movieService.getMoviesByLanguage(language));
    }

    @GetMapping("getmoviesbytitle")
    public ResponseEntity<?> getMoviesByTitle(@RequestParam String title){
        return ResponseEntity.ok(movieService.getMoviesByTitle(title));
    }


    //admin only operations
    @PostMapping("/addmovie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addMovie(@RequestBody MovieDto movieDTO) {

        return ResponseEntity.ok(movieService.addMovie(movieDTO));
    }

    @PutMapping("/updatemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateMovie(@PathVariable Long id,@RequestBody MovieDto movieDTO){
        return ResponseEntity.ok(movieService.updateMovie(id, movieDTO));
    }

    @DeleteMapping("/deletemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }



}
