package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.TheaterDto;
import com.example.MovieBooking.entity.Theater;
import com.example.MovieBooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    private final ModelMapper modelMapper;

    public TheaterDto addTheater(TheaterDto theaterDto) {
        Theater theater=modelMapper.map(theaterDto,Theater.class);
        Theater savedTheater=theaterRepository.save(theater);
        return modelMapper.map(savedTheater, TheaterDto.class);
    }

    public TheaterDto updateTheater(Long id, TheaterDto theaterDto) {
        Theater theater=theaterRepository.findById(id).orElseThrow(()->new RuntimeException("Theater not found"));
        theater.setName(theaterDto.getName());
        theater.setAddress(theaterDto.getAddress());
        theater.setCity(theaterDto.getCity());
        theater.setLatitude(theaterDto.getLatitude());
        theater.setLongitude(theaterDto.getLongitude());
        Theater updatedTheater=theaterRepository.save(theater);
        return modelMapper.map(updatedTheater, TheaterDto.class);
    }

    public void deleteTheater(Long id) {
        theaterRepository.deleteById(id);
    }

    public List<TheaterDto> findTheatersByCity(String city) {
        List<Theater> theaters=theaterRepository.findByCity(city);
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterDto.class))
                .toList();
    }

    public List<TheaterDto> getAllTheaters() {
        List<Theater> theaters=theaterRepository.findAll();
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterDto.class))
                .toList();
    }

}
