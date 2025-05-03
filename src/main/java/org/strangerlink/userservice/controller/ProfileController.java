package org.strangerlink.userservice.controller;

import org.strangerlink.userservice.dto.ProfileDto.ProfileRequest;
import org.strangerlink.userservice.dto.ProfileDto.ProfileResponse;
import org.strangerlink.userservice.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(profileService.getCurrentUserProfile());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest profileRequest) {
        return ResponseEntity.ok(profileService.updateProfile(profileRequest));
    }

    @PostMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadProfileImage(file));
    }
}