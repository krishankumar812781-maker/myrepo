package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.JwtAuthResponseDto;

public interface AuthService {
    JwtAuthResponseDto login(LoginDto loginDto);
    String register(RegisterDto registerDto);

    JwtAuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto);
}