package org.strangerlink.service;

import org.springframework.transaction.annotation.Transactional;
import org.strangerlink.dto.ProfileDto.ProfileSearchRequest;
import org.strangerlink.model.Profile;
import org.strangerlink.model.SearchPreference;
import org.strangerlink.model.User;
import org.strangerlink.repository.ProfileRepository;
import org.strangerlink.repository.SearchPreferenceRepository;
import org.strangerlink.repository.UserRepository;
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
    private final SearchPreferenceRepository searchPreferenceRepository;

    public List<User> searchUsers(ProfileSearchRequest searchRequest) {
        // Get current user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's search preferences
        SearchPreference pref = searchPreferenceRepository.findByUserId(currentUser.getId())
                .orElse(null);

        Integer minAge = searchRequest.getAge();
        Integer maxAge = searchRequest.getAge();
        String gender = searchRequest.getGender();
        Long countryId = searchRequest.getCountryId();
        boolean allCountries = true;

        // Override with user preferences if requested
        if (searchRequest.isUsePreferences() && pref != null) {
            minAge = pref.getMinAge();
            maxAge = pref.getMaxAge();
            gender = "all".equals(pref.getPreferredGender()) ? null : pref.getPreferredGender();
            countryId = pref.isAllCountries() ? null :
                    (pref.getPreferredCountry() != null ? pref.getPreferredCountry().getId() : null);
            allCountries = pref.isAllCountries();
        }

        // Search for users based on criteria
        return userRepository.findMatchingUsers(
                minAge,
                maxAge,
                gender,
                countryId,
                allCountries,
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