package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.JwtAuthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    JwtAuthResponseDto login(LoginDto loginDto);
    String register(RegisterDto registerDto);

    JwtAuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);

    ResponseEntity<JwtAuthResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId);
}