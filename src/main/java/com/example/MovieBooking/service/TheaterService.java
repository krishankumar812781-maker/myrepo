package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.NominatimResponseDto; // You need to create this DTO
import com.example.MovieBooking.dto.RequestDto.TheaterRequestDto; // Renamed from TheaterDto
import com.example.MovieBooking.dto.TheaterResponseDto; // You need to create this DTO
import com.example.MovieBooking.entity.Theater;
import com.example.MovieBooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate; // 1. Inject RestTemplate

    private static final Logger LOGGER = LoggerFactory.getLogger(TheaterService.class); // 2. Add Logger

    /**
     * Adds a new theater and geocodes its address.
     */
    @Transactional
    public TheaterResponseDto addTheater(TheaterRequestDto requestDto) {
        // 3. Manually map request DTO to entity
        Theater theater = new Theater();
        theater.setName(requestDto.getName());
        theater.setAddress(requestDto.getAddress());
        theater.setCity(requestDto.getCity());

        // 4. Call Geocoding API
        callGeocodingApi(theater);

        Theater savedTheater = theaterRepository.save(theater);
        return modelMapper.map(savedTheater, TheaterResponseDto.class);
    }

    /**
     * Updates an existing theater.
     */
    @Transactional
    public TheaterResponseDto updateTheater(Long id, TheaterRequestDto requestDto) {
        // 5. Find the existing theater
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        // 6. Fix logic: Update the fields of the *existing* theater
        theater.setName(requestDto.getName());
        theater.setAddress(requestDto.getAddress());
        theater.setCity(requestDto.getCity());

        // (Optional: You could check if the address changed and re-geocode)
        // For now, we'll just update the simple fields.

        Theater updatedTheater = theaterRepository.save(theater); // Save the updated object
        return modelMapper.map(updatedTheater, TheaterResponseDto.class);
    }

    /**
     * Deletes a theater.
     */
    @Transactional
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new RuntimeException("Theater not found with id: " + id);
        }
        // (Note: This will fail if the theater has screens, which is good!)
        theaterRepository.deleteById(id);
    }

    /**
     * Finds theaters by city (read-only).
     */
    @Transactional(readOnly = true)
    public List<TheaterResponseDto> findTheatersByCity(String city) {
        List<Theater> theaters = theaterRepository.findByCity(city);
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterResponseDto.class))
                .toList();
    }

    /**
     * Gets all theaters (read-only).
     */
    @Transactional(readOnly = true)
    public List<TheaterResponseDto> getAllTheaters() {
        List<Theater> theaters = theaterRepository.findAll();
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterResponseDto.class))
                .toList();
    }

    /**
     * Private helper method to call the Nominatim API.
     */
    private void callGeocodingApi(Theater theater) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                    .queryParam("q", theater.getAddress() + ", " + theater.getCity())
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MovieBookingApp-v1.0 (your-email@example.com)");
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimResponseDto[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NominatimResponseDto[].class
            );

            if (response.getBody() != null && response.getBody().length > 0) {
                NominatimResponseDto geo = response.getBody()[0];
                theater.setLatitude(Double.parseDouble(geo.getLatitude()));
                theater.setLongitude(Double.parseDouble(geo.getLongitude()));
                LOGGER.info("Geocoding successful for: {}", theater.getAddress());
            } else {
                LOGGER.warn("Could not find coordinates for address: {}", theater.getAddress());
            }
        } catch (Exception e) {
            LOGGER.error("Geocoding API failed: {}", e.getMessage());
        }
    }
}