package org.strangerlink.userservice.service;

import org.strangerlink.userservice.dto.CountryDto;
import org.strangerlink.userservice.dto.ProfileDto.ProfileRequest;
import org.strangerlink.userservice.dto.ProfileDto.ProfileResponse;
import org.strangerlink.userservice.model.Country;
import org.strangerlink.userservice.model.Interest;
import org.strangerlink.userservice.model.Profile;
import org.strangerlink.userservice.model.User;
import org.strangerlink.userservice.repository.CountryRepository;
import org.strangerlink.userservice.repository.InterestRepository;
import org.strangerlink.userservice.repository.ProfileRepository;
import org.strangerlink.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private InterestRepository interestRepository;
    private CountryRepository countryRepository;

    private final Path uploadDir = Paths.get("uploads/profile-images");

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository,
                          InterestRepository interestRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.interestRepository = interestRepository;
    }

    // Metodo per aggiungere interessi a un profilo
    public ProfileResponse addInterestsToProfile(Long userId, List<String> interestNames) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        for (String interestName : interestNames) {
            // Cerca l'interesse esistente o creane uno nuovo
            Interest interest = interestRepository.findByName(interestName)
                    .orElseGet(() -> {
                        Interest newInterest = new Interest();
                        newInterest.setName(interestName);
                        return interestRepository.save(newInterest);
                    });

            profile.addInterest(interest);
        }

        Profile updatedProfile = profileRepository.save(profile);
        return mapToProfileResponse(updatedProfile);
    }

    // Modifica questo metodo per gestire la lista di interessi
    private ProfileResponse mapToProfileResponse(Profile profile) {
        List<String> interestNames = profile.getInterests().stream()
                .map(Interest::getName)
                .collect(Collectors.toList());

        CountryDto countryDto = null;
        if (profile.getCountry() != null) {
            countryDto = new CountryDto(
                    profile.getCountry().getId(),
                    profile.getCountry().getName(),
                    profile.getCountry().getCode()
            );
        }

        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .displayName(profile.getDisplayName())
                .age(profile.getAge())
                .country(countryDto)
                .gender(profile.getGender())
                .bio(profile.getBio())
                .profileImageUrl(profile.getProfileImageUrl())
                .interests(interestNames)
                .build();
    }


    public ProfileResponse getCurrentUserProfile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToProfileResponse(profile);
    }

    public ProfileResponse getProfileById(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToProfileResponse(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(ProfileRequest profileRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profileRequest.getDisplayName() != null) {
            profile.setDisplayName(profileRequest.getDisplayName());
        }

        if (profileRequest.getAge() != null) {
            profile.setAge(profileRequest.getAge());
        }

        if (profileRequest.getCountryId() != null) {
            Country country = countryRepository.findById(profileRequest.getCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found"));
            profile.setCountry(country);
        }

        if (profileRequest.getGender() != null) {
            profile.setGender(profileRequest.getGender());
        }

        if (profileRequest.getBio() != null) {
            profile.setBio(profileRequest.getBio());
        }

        if (profileRequest.getInterests() != null) {
            // Rimuovi interessi esistenti
            profile.getInterests().clear();

            // Aggiungi nuovi interessi
            for (String interestName : profileRequest.getInterests()) {
                Interest interest = interestRepository.findByName(interestName)
                        .orElseGet(() -> {
                            Interest newInterest = new Interest();
                            newInterest.setName(interestName);
                            return interestRepository.save(newInterest);
                        });

                profile.addInterest(interest);
            }
        }

        Profile updatedProfile = profileRepository.save(profile);
        return mapToProfileResponse(updatedProfile);
    }

    @Transactional
    public ProfileResponse uploadProfileImage(MultipartFile file) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        try {
            String filename = UUID.randomUUID().toString() +
                    getExtension(file.getOriginalFilename());

            Path targetLocation = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation);

            profile.setProfileImageUrl("/uploads/profile-images/" + filename);
            Profile updatedProfile = profileRepository.save(profile);

            return mapToProfileResponse(updatedProfile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store profile image", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }

}