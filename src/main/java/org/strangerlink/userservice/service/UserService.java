package org.strangerlink.userservice.service;

import org.springframework.transaction.annotation.Transactional;
import org.strangerlink.userservice.dto.ProfileDto.ProfileSearchRequest;
import org.strangerlink.userservice.model.Profile;
import org.strangerlink.userservice.model.User;
import org.strangerlink.userservice.repository.ProfileRepository;
import org.strangerlink.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public List<User> searchUsers(ProfileSearchRequest searchRequest) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Search for users based on criteria
        return userRepository.findMatchingUsers(
                searchRequest.getAge(),
                searchRequest.getCountry(),
                searchRequest.getGender(),
                currentUser.getId()
        );
    }

    public void blockUser(Long userId) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user to block
        User userToBlock = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to block not found"));

        // Add to blocked users
        currentUser.getBlockedUsers().add(userToBlock);
        userRepository.save(currentUser);
    }

    public void unblockUser(Long userId) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user to unblock
        User userToUnblock = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to unblock not found"));

        // Remove from blocked users
        currentUser.getBlockedUsers().remove(userToUnblock);
        userRepository.save(currentUser);
    }

    public List<User> getBlockedUsers() {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return blocked users
        return currentUser.getBlockedUsers().stream().toList();
    }

    @Transactional
    public User processOAuthUser(String email, String name, String provider) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Crea un nuovo utente
        User newUser = new User();
        newUser.setEmail(email);
        // Genera username basato sul nome o email
        newUser.setUsername(generateUniqueUsername(name));
        newUser.setPassword(""); // Password vuota per utenti OAuth
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastActive(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        // Crea un profilo base
        Profile profile = new Profile();
        profile.setUser(savedUser);
        profile.setDisplayName(name);
        profileRepository.save(profile);

        return savedUser;
    }

    private String generateUniqueUsername(String name) {
        // Rimuovi spazi e caratteri speciali, aggiungi numero casuale
        String baseName = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String username = baseName;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseName + counter;
            counter++;
        }

        return username;
    }
}