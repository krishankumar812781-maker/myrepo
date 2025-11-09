package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.ShowRequestDto;
import com.example.MovieBooking.dto.ShowResponseDto;
import com.example.MovieBooking.dto.RequestDto.ShowUpdateRequestDto;
import com.example.MovieBooking.dto.ShowSeatDto;
import com.example.MovieBooking.entity.*;
import com.example.MovieBooking.entity.type.SeatStatus;
import com.example.MovieBooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;

    private final ScreenRepository screenRepository;

    private final MovieRepository movieRepository;

    private final SeatRepository seatRepository;

    private final ShowSeatRepository showSeatRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final BookingRepository bookingRepository;

    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<ShowSeatDto> getSeatsForShow(Long showId) {
        // ... (your existsById check) ...

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);

        // 3. Map to DTOs (The new way)
        return showSeats.stream()
                .map(showSeat -> modelMapper.map(showSeat, ShowSeatDto.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowResponseDto> getShowsByTheaterId(Long theaterId) {
        // Step 1: Find all screens belonging to the given theater
        List<Screen> screens=screenRepository.findByTheaterId(theaterId);

        if (screens.isEmpty()) {
            // Or throw a custom TheaterNotFoundException if theaterId doesn't exist at all
            return new ArrayList<>(); // Return empty list if no screens or theater not found
        }

        // Step 2: For each screen, find all shows
        List<Show> allShows = screens.stream()
                .flatMap(screen -> showRepository.findByScreenId(screen.getId()).stream())
                .toList();

        // Step 3: Convert entities to DTOs and return
        //here show is like i in loop it is to represent the single item from the list that is being processed right now
        return allShows.stream()
                .map(this::mapToShowResponseDto)
                .toList();
    }

    @Transactional
    public ShowResponseDto createShow(ShowRequestDto showRequestDto) {

            // --- 1. MANUAL MAPPING (DTO to Entity) ---
            // We cannot use ModelMapper here. We must fetch the relations.

            Movie movie = movieRepository.findById(showRequestDto.getMovieId())
                    .orElseThrow(() -> new RuntimeException("Movie not found with id: " + showRequestDto.getMovieId()));

            Screen screen = screenRepository.findById(showRequestDto.getScreenId())
                    .orElseThrow(() -> new RuntimeException("Screen not found with id: " + showRequestDto.getScreenId()));

            Show show = new Show();
            show.setStartTime(showRequestDto.getStartTime());
            show.setEndTime(showRequestDto.getEndTime());
            show.setMovie(movie);
            show.setScreen(screen);

            // Save the Show first to get its generated ID
            Show savedShow = showRepository.save(show);

            // --- 2. GENERATE SHOWSEAT INVENTORY  ---

            // Get the price map and template seats- and create ShowSeats
            Map<String, BigDecimal> seatPriceConfig = showRequestDto.getSeatPrices();
            List<Seat> templateSeats = seatRepository.findByScreenId(screen.getId());

            List<ShowSeat> showSeatsToSave = templateSeats.stream()
                    .map(seat -> {
                        //ye sab 'Seat' pa iterate ho raha hai and we are creating 'ShowSeat'
                        //seat is like i

                        BigDecimal price = seatPriceConfig.get(seat.getSeatType());
                        if (price == null) {
                            throw new RuntimeException("Price not configured for seat type: " + seat.getSeatType());
                        }

                        ShowSeat showSeat = new ShowSeat();
                        showSeat.setShow(savedShow); // Link to the Show we just saved
                        showSeat.setSeat(seat);
                        showSeat.setStatus(SeatStatus.AVAILABLE); //enum set krne ke lia
                        showSeat.setPrice(price);
                        return showSeat;
                    })
                    .toList();

            // Save all the new ShowSeats to the database in one batch
            showSeatRepository.saveAll(showSeatsToSave);

            // --- 3. MAPPING (Entity to DTO) ---
            // We use ModelMapper for simple fields, then manually set the rest.

           return mapToShowResponseDto(savedShow);
    }


    @Transactional(readOnly = true)
    public List<ShowResponseDto> getAllShows() {
        return showRepository.findAll()
                .stream()
                .map(this::mapToShowResponseDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ShowResponseDto> getShowsByMovieId(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        List<Show> shows = showRepository.findShowByMovieId(movieId);

        return shows.stream()
                .map(this::mapToShowResponseDto)
                .toList();

    }

    @Transactional
    public ShowResponseDto updateShow(Long showId, ShowUpdateRequestDto updateDto) {
        // 1. Find the Show to update
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));

        // 2. Update the simple fields
        show.setStartTime(updateDto.getStartTime());
        show.setEndTime(updateDto.getEndTime());

        // 3. Handle the optional price update
        Map<String, BigDecimal> priceConfig = updateDto.getSeatPrices();

        // Only update prices if the admin provided a price map
        if (priceConfig != null && !priceConfig.isEmpty()) {

            // Find all seats for this show
            List<ShowSeat> showSeats = showSeatRepository.findByShowId(showId);

            // Use a stream to update only AVAILABLE seats
            showSeats.stream()
                    .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                    .forEach(seat -> {
                        // Get the seat's type (e.g., "PREMIUM")
                        String seatType = seat.getSeat().getSeatType();

                        // Find the new price for that type from the DTO
                        BigDecimal newPrice = priceConfig.get(seatType);

                        // If a new price is provided for this type, update it
                        if (newPrice != null) {
                            seat.setPrice(newPrice);
                        }
                    });

            // Because we are in a @Transactional method,
            // JPA will automatically save the changes to the ShowSeat entities.
        }

        // 4. Save the updated Show entity
        Show savedShow = showRepository.save(show);

        // --- PUBLISH KAFKA EVENT ---
        // Create a message to send. Using JSON as a string is a good practice.
        String message = String.format(
                "{\"showId\": %d, \"newStartTime\": \"%s\"}",
                savedShow.getId(),
                savedShow.getStartTime().toString()
        );
        kafkaTemplate.send("show-updated-topic", message);

        // 5. Map to a response DTO and return
        return mapToShowResponseDto(savedShow); // Use your existing helper method
    }

    @Transactional
    public void deleteShow(Long showId) {

        if (!showRepository.existsById(showId)) {
            throw new RuntimeException("Show not found with id: " + showId);
        }

        // CRITICAL: Check for any "BOOKED" seats for this show.
        // We use "BOOKED" as an example status.
        boolean hasBookedSeats = showSeatRepository.existsByShowIdAndStatus(showId, "BOOKED");

        if (hasBookedSeats) {
            throw new RuntimeException("Cannot delete show: It has active bookings.");
        }
        // deleting the Show will cascade and automatically delete all of its associated ShowSeat inventory.
        showRepository.deleteById(showId);
    }

    /**
     * Helper method to map a Show entity to a ShowResponseDto.
     */
    private ShowResponseDto mapToShowResponseDto(Show show) {
        ShowResponseDto dto = modelMapper.map(show, ShowResponseDto.class);
        dto.setMovieTitle(show.getMovie().getTitle());
        dto.setMoviePosterUrl(show.getMovie().getPosterUrl());
        dto.setScreenName(show.getScreen().getName());
        dto.setTheaterName(show.getScreen().getTheater().getName());
        return dto;
    }

    }


