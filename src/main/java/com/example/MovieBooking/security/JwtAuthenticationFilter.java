package com.example.MovieBooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;


    private final UserDetailsService userDetailsService; // Your CustomUserDetailsService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Get JWT token from the request
        String token = getJwtFromRequest(request);

        // 2. Validate the token
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {

            // 3. Get username (email) from token
            String username = tokenProvider.getUsername(token);

            // 4. Load the user associated with the token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Create an authentication token
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 6. Set the user in the Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    // Helper method to get the token from the "Authorization" header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Return just the token part
        }
        return null;
    }
}
