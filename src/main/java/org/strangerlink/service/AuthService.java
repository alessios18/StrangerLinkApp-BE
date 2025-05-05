package org.strangerlink.service;

import org.springframework.http.HttpStatus;
import org.strangerlink.dto.AuthDto.AuthResponse;
import org.strangerlink.dto.AuthDto.LoginRequest;
import org.strangerlink.dto.AuthDto.RegisterRequest;
import org.strangerlink.dto.UserDto;
import org.strangerlink.exception.ApiException;
import org.strangerlink.model.Profile;
import org.strangerlink.model.User;
import org.strangerlink.repository.ProfileRepository;
import org.strangerlink.repository.UserRepository;
import org.strangerlink.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username already taken");
        }
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email already in use");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        user.setLastActive(LocalDateTime.now(ZoneOffset.UTC));

        User savedUser = userRepository.save(user);

        // Create empty profile
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setDisplayName(request.getUsername());
        profileRepository.save(profile);

        // Generate JWT token
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(
                        savedUser.getUsername(),
                        savedUser.getPassword(),
                        savedUser.isEnabled(),
                        true,
                        true,
                        true,
                        java.util.Collections.emptyList()
                )
        );

        // Crea UserDTO
        UserDto userDTO = UserDto.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .createdAt(savedUser.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .lastActive(savedUser.getLastActive().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();

        // Restituisci AuthResponse con il token e l'oggetto UserDTO
        return AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // Find our entity user
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update lastActive, NOT createdAt
        user.setLastActive(LocalDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        // Generate JWT token
        String token = jwtService.generateToken(principal);

        // Create UserDTO
        UserDto userDTO = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli())
                .lastActive(user.getLastActive().toInstant(ZoneOffset.UTC).toEpochMilli())
                .build();

        // Return AuthResponse with token and UserDTO
        return AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();
    }
}