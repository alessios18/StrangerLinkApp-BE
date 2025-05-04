package org.strangerlink.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.dto.CountryDto;
import org.strangerlink.model.Country;
import org.strangerlink.service.CountryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    public ResponseEntity<List<CountryDto>> getAllCountries() {
        List<Country> countries = countryService.getAllCountries();
        List<CountryDto> countryDtos = countries.stream()
                .map(country -> new CountryDto(
                        country.getId(),
                        country.getName(),
                        country.getCode()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(countryDtos);
    }
}