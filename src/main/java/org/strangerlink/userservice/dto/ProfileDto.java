package org.strangerlink.userservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ProfileDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileRequest {
        @Size(max = 50, message = "Display name cannot exceed 50 characters")
        private String displayName;

        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be less than 120")
        private Integer age;

        private String country;

        private String gender;

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        private String bio;

        private List<String> interests;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileResponse {
        private Long id;
        private Long userId;
        private String displayName;
        private Integer age;
        private String country;
        private String gender;
        private String bio;
        private String profileImageUrl;
        private List<String> interests;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfileSearchRequest {
        private Integer age;
        private String country;
        private String gender;
    }
}