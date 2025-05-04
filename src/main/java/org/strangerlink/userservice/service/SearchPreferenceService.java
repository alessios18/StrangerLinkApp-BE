package org.strangerlink.userservice.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strangerlink.userservice.dto.SearchPreferenceDto;
import org.strangerlink.userservice.model.Country;
import org.strangerlink.userservice.model.SearchPreference;
import org.strangerlink.userservice.model.User;
import org.strangerlink.userservice.repository.CountryRepository;
import org.strangerlink.userservice.repository.SearchPreferenceRepository;
import org.strangerlink.userservice.repository.UserRepository;

@Service
public class SearchPreferenceService {

    private final SearchPreferenceRepository searchPreferenceRepository;
    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    public SearchPreferenceService(
            SearchPreferenceRepository searchPreferenceRepository,
            UserRepository userRepository,
            CountryRepository countryRepository) {
        this.searchPreferenceRepository = searchPreferenceRepository;
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
    }

    public SearchPreferenceDto getUserSearchPreferences() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SearchPreference pref = searchPreferenceRepository.findByUserId(user.getId())
                .orElse(new SearchPreference());

        return convertToDto(pref);
    }

    @Transactional
    public SearchPreferenceDto updateSearchPreferences(SearchPreferenceDto dto) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SearchPreference pref = searchPreferenceRepository.findByUserId(user.getId())
                .orElse(new SearchPreference());

        pref.setUser(user);
        pref.setMinAge(dto.getMinAge());
        pref.setMaxAge(dto.getMaxAge());
        pref.setPreferredGender(dto.getPreferredGender());
        pref.setAllCountries(dto.isAllCountries());

        if (!dto.isAllCountries() && dto.getPreferredCountryId() != null) {
            Country country = countryRepository.findById(dto.getPreferredCountryId())
                    .orElseThrow(() -> new RuntimeException("Country not found"));
            pref.setPreferredCountry(country);
        } else {
            pref.setPreferredCountry(null);
        }

        SearchPreference savedPref = searchPreferenceRepository.save(pref);
        return convertToDto(savedPref);
    }

    private SearchPreferenceDto convertToDto(SearchPreference pref) {
        return SearchPreferenceDto.builder()
                .minAge(pref.getMinAge())
                .maxAge(pref.getMaxAge())
                .preferredGender(pref.getPreferredGender())
                .preferredCountryId(pref.getPreferredCountry() != null ? pref.getPreferredCountry().getId() : null)
                .allCountries(pref.isAllCountries())
                .build();
    }
}