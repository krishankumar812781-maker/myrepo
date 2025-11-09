package com.example.MovieBooking.controller;

import com.example.MovieBooking.dto.JwtAuthResponseDto;
import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint for user login.
     * Takes a LoginDto (username/email and password) and returns a JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> login(@RequestBody LoginDto loginDto) {
         JwtAuthResponseDto jwtAuthResponse = authService.login(loginDto);
        return new ResponseEntity<>(jwtAuthResponse,HttpStatus.OK);
    }

    /**
     * Endpoint for new user registration.
     * Takes a RegisterDto and creates a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        String response = authService.register(registerDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // --- ADD NEW ENDPOINT FOR REFRESHING ---
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        JwtAuthResponseDto jwtAuthResponse = authService.refreshToken(refreshTokenRequestDto);
        return new ResponseEntity<>(jwtAuthResponse,HttpStatus.OK);
    }
}