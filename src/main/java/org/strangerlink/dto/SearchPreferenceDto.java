package org.strangerlink.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchPreferenceDto {
    @Min(value = 18, message = "Minimum age must be at least 18")
    private Integer minAge;

    @Max(value = 120, message = "Maximum age must be less than 120")
    private Integer maxAge;

    private String preferredGender; // "male", "female", "all"

    private Long preferredCountryId;

    private boolean allCountries;
}
