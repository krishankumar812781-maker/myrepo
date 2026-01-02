package com.example.MovieBooking.service;

import com.example.MovieBooking.dto.RequestDto.LoginDto;
import com.example.MovieBooking.dto.RequestDto.LoginResponseDto;
import com.example.MovieBooking.dto.RequestDto.RefreshTokenRequestDto;
import com.example.MovieBooking.dto.RequestDto.RegisterDto;
import com.example.MovieBooking.dto.JwtAuthResponseDto;
import com.example.MovieBooking.entity.User;
import com.example.MovieBooking.entity.type.AuthProvider;
import com.example.MovieBooking.entity.type.Role;
import com.example.MovieBooking.repository.UserRepository;
import com.example.MovieBooking.security.JwtTokenProvider;
import com.example.MovieBooking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.security.auth.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Authenticates a user and returns a JWT.
     */
    @Override
    @Transactional
    public JwtAuthResponseDto login(LoginDto loginDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // --- Generate BOTH tokens ---
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // --- Save the refresh token to the user in the DB ---
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(refreshToken).toInstant());
        userRepository.save(user);

        return new JwtAuthResponseDto(accessToken, refreshToken);
    }

    /**
     * Registers a new user.
     */
    @Override
    @Transactional
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

        try {
            kafkaTemplate.send("user-registered-topic", user.getEmail());
        } catch (Exception e) {
            // Log the error, but don't fail the registration
            // (This is "fire and forget")
            LOGGER.warn("Failed to send user registration to Kafka, but user was created. User: {}", user.getEmail(), e);
        }

        return "User registered successfully.";
    }


    // --- IMPLEMENT NEW REFRESH TOKEN METHOD ---
    //1. You receive the refreshToken and extract the email.
    //2. You go to the userRepository to find that user.
    //3. You get the roles (e.g., ROLE_ADMIN) from that user entity.
    //4. You call generateAccessTokenFromEmail(email, roles) to create the new token with the role baked in.
    @Override
    @Transactional
    public JwtAuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        String requestRefreshToken = refreshTokenRequestDto.getRefreshToken();

        // 1. Find user by the refresh token
        User user = userRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found."));

        // 2. Check if the token is expired
        if (user.getRefreshTokenExpiry().isBefore(Instant.now())) {
            // Clean up the expired token
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh token has expired. Please log in again.");
        }

        // --- 3. GENERATE *BOTH* NEW TOKENS (This is the change) ---
        // ⚡ CHANGE 1: Convert roles to a string for the JWT
        String rolesStr = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        // ⚡ CHANGE 2: Pass the roles string to the provider
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), rolesStr);
        String newRefreshToken = jwtTokenProvider.generateRefreshTokenFromEmail(user.getEmail(), rolesStr);// <-- NEW

        // --- 4. SAVE THE *NEW* REFRESH TOKEN TO THE DATABASE ---
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(newRefreshToken).toInstant());
        userRepository.save(user);

        // --- 5. RETURN BOTH *NEW* TOKENS ---
        return new JwtAuthResponseDto(newAccessToken, newRefreshToken);
    }

    //Add roles here also while generating tokens for OAuth2 login
    @Override
    @Transactional
    public ResponseEntity<JwtAuthResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {

        // 1. Determine provider type
        AuthProvider providerType = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            case "facebook" -> AuthProvider.FACEBOOK;
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };

        // 2. Extract unique Provider ID and Email
        String providerId = registrationId.equalsIgnoreCase("google")
                ? oAuth2User.getAttribute("sub")
                : oAuth2User.getAttribute("id").toString();

        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email not provided by " + registrationId);
        }

        // 3. Logic: Find existing user or create new one
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // --- NEW USER REGISTRATION ---
            user = new User();
            user.setEmail(email);
            user.setUsername(determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId));
            user.setAuthProvider(providerType);
            user.setProviderId(providerId);
            user.setPassword(""); // OAuth users don't need a local password

            Set<Role> roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
            user.setRoles(roles);

            user = userRepository.save(user);
            log.info("Registered new OAuth2 user: {}", email);
        } else {
            // --- EXISTING USER LOGIC ---
            // Check if they previously signed up with a different provider (e.g., Local or GitHub)
            if (user.getAuthProvider() != providerType && user.getAuthProvider() != AuthProvider.LOCAL) {
                throw new BadCredentialsException("This email is already registered via " + user.getAuthProvider());
            }

            // Update provider info if it was a local account transitioning to OAuth
            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
                user.setAuthProvider(providerType);
                userRepository.save(user);
            }
        }

        // 4. Generate Tokens (Matching your Login method logic)
        String rolesStr = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        // ⚡ CHANGE 4: Pass roles here too
        String accessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), rolesStr);
        String refreshToken = jwtTokenProvider.generateRefreshTokenFromEmail(user.getEmail(), rolesStr);


        // 5. Update Refresh Token in DB
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(jwtTokenProvider.getExpiryDateFromToken(refreshToken).toInstant());
        userRepository.save(user);

        return ResponseEntity.ok(new JwtAuthResponseDto(accessToken, refreshToken));
    }

    public String determineUsernameFromOAuth2User(OAuth2User oAuth2User, String registrationId, String providerId) {
        //jarurui nahi hai ki Auth2Provider hame user ki email provide kr de so hame aur kuch fill kr denge
        String email = oAuth2User.getAttribute("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return switch (registrationId.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }

}