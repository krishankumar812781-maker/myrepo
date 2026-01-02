package com.example.MovieBooking.security;

import com.example.MovieBooking.dto.JwtAuthResponseDto;
import com.example.MovieBooking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy; // Added for Lazy injection
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Auth2SucessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    // Use manual constructor with @Lazy to break the circular dependency
    public Auth2SucessHandler(@Lazy AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. Cast the authentication to get OAuth2 details
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 2. Get the registration ID (e.g., "google")
        String registrationId = token.getAuthorizedClientRegistrationId();

        // 3. Call your service to handle DB logic and token generation
        ResponseEntity<JwtAuthResponseDto> loginResponse = authService.handleOAuth2LoginRequest(oAuth2User, registrationId);

        // 4. Redirect to React instead of writing JSON
        if (loginResponse.getBody() != null) {
            JwtAuthResponseDto authData = loginResponse.getBody();

            // Construct the URL for your React "GoogleCallback" component
            String targetUrl = "http://localhost:5173/oauth2/callback?" +
                    "token=" + authData.getAccessToken() +
                    "&refreshToken=" + authData.getRefreshToken();

            // Perform the redirect
            response.sendRedirect(targetUrl);
        } else {
            // If login failed, send back to your login page with an error
            response.sendRedirect("http://localhost:5173/login?error=auth_failed");
        }
    }
}