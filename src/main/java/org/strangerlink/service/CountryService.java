package org.strangerlink.service;

import org.springframework.stereotype.Service;
import org.strangerlink.model.Country;
import org.strangerlink.repository.CountryRepository;

import java.util.List;

@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    public Country getCountryByCode(String code) {
        return countryRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Country not found with code: " + code));
    }
}