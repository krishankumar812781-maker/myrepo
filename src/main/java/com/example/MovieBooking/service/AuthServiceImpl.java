package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.JwtAuthResponseDto;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import com.example.MovieBooking.repository.UserRepository;
import com.example.MovieBooking.security.JwtTokenProvider;
import com.example.MovieBooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticates a user and returns a JWT.
     */
    @Override
    public JwtAuthResponseDto login(LoginDto loginDto) {

        // 1. Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        // 2. Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the JWT
        String token = jwtTokenProvider.generateToken(authentication);

        return new JwtAuthResponseDto(token);
    }

    /**
     * Registers a new user.
     */
    @Override
    public String register(RegisterDto registerDto) {

        // 1. Check if username or email already exists
        if (userRepository.findByUsername(registerDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already taken!");
        }

        // 2. Create new User object
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setAuthProvider(AuthProvider.LOCAL); // Local registration

        // 3. Set roles
        Set<Role> roles = new HashSet<>();

        if (registerDto.getRoles() != null && !registerDto.getRoles().isEmpty()) {
            roles = registerDto.getRoles().stream()
                    .map(roleStr -> {
                        try {
                            return Role.valueOf(roleStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid role: " + roleStr);
                        }
                    })
                    .collect(Collectors.toSet());
        } else {
            // default role
            roles.add(Role.ROLE_USER);
        }

        user.setRoles(roles);

        // 4. Save user to database
        userRepository.save(user);

        return "User registered successfully.";
    }
}