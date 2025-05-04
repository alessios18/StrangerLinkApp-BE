// src/main/java/org/strangerlink/userservice/controller/SearchController.java
package org.strangerlink.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.dto.ProfileDto.ProfileSearchRequest;
import org.strangerlink.dto.UserDto;
import org.strangerlink.model.User;
import org.strangerlink.repository.UserRepository;
import org.strangerlink.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/users")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestBody ProfileSearchRequest searchRequest) {
        // Get current user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set the flag to use the user's saved search preferences
        searchRequest.setUsePreferences(true);

        // Get matching users based on search criteria and preferences
        List<User> matchingUsers = userService.searchUsers(searchRequest);

        // Convert to DTOs
        List<UserDto> userDtos = matchingUsers.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                        .lastActive(user.getLastActive().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/random-match")
    public ResponseEntity<?> getRandomMatch() {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find random match
        User matchedUser = userService.findRandomMatch(currentUser.getId());

        if (matchedUser == null) {
            return ResponseEntity.noContent().build(); // No match found
        }

        // Convert to DTO
        UserDto userDto = UserDto.builder()
                .id(matchedUser.getId())
                .username(matchedUser.getUsername())
                .email(matchedUser.getEmail())
                .createdAt(matchedUser.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                .lastActive(matchedUser.getLastActive().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                .build();

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<UserDto>> getRecentlyActiveUsers() {
        // Get current user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get recently active users (last 24 hours)
        java.time.LocalDateTime cutoffTime = java.time.LocalDateTime.now().minusHours(24);
        List<User> recentUsers = userRepository.findOnlineUsers(cutoffTime).stream()
                .filter(user -> !user.getId().equals(currentUser.getId())) // Exclude current user
                .collect(Collectors.toList());

        // Convert to DTOs
        List<UserDto> userDtos = recentUsers.stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                        .lastActive(user.getLastActive().toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }
}