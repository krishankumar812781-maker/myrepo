package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.RequestDto.MovieDto;
import com.example.MovieBooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // --- Public Endpoints ---

    @GetMapping("/getallmovies")
    public ResponseEntity<?> getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/getmoviesbygenre") // Fixed: Added leading slash
    public ResponseEntity<?> getMoviesByGenre(@RequestParam String genre){
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @GetMapping("/getmoviesbylanguage") // Fixed: Added leading slash
    public ResponseEntity<?> getMoviesByLanguage(@RequestParam String language){
        return ResponseEntity.ok(movieService.getMoviesByLanguage(language));
    }

    @GetMapping("/getmoviesbytitle") // Fixed: Added leading slash
    public ResponseEntity<?> getMoviesByTitle(@RequestParam String title){
        return ResponseEntity.ok(movieService.getMoviesByTitle(title));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    // --- Admin Only Operations ---

    @PostMapping("/addmovie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addMovie(@RequestBody MovieDto movieDTO) {
        return ResponseEntity.ok(movieService.addMovie(movieDTO));
    }

    @DeleteMapping("/deletemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }

    @PutMapping("/updatemovie/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody MovieDto movieDTO){
        return ResponseEntity.ok(movieService.updateMovie(id, movieDTO));
    }



    // --- NEW: OMDb External API Endpoints ---

    /**
     * Search OMDb for movies by title before adding them to our DB.
     * GET /api/movies/omdb/search?title=Batman
     */
    @GetMapping("/omdb/search")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> searchOmdb(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchOmdb(title));
    }

    /**
     * Import a movie from OMDb using its imdbId.
     * POST /api/movies/omdb/import/tt1234567
     */
    @PostMapping("/omdb/import/{imdbId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> importMovie(@PathVariable String imdbId) {
        return new ResponseEntity<>(movieService.importMovieByImdbId(imdbId), HttpStatus.CREATED);
    }
}