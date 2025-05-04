package org.strangerlink.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.userservice.dto.SearchPreferenceDto;
import org.strangerlink.userservice.service.SearchPreferenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/search-preferences")
public class SearchPreferenceController {

    private final SearchPreferenceService searchPreferenceService;

    public SearchPreferenceController(SearchPreferenceService searchPreferenceService) {
        this.searchPreferenceService = searchPreferenceService;
    }

    @GetMapping
    public ResponseEntity<SearchPreferenceDto> getSearchPreferences() {
        return ResponseEntity.ok(searchPreferenceService.getUserSearchPreferences());
    }

    @PutMapping
    public ResponseEntity<SearchPreferenceDto> updateSearchPreferences(
            @Valid @RequestBody SearchPreferenceDto searchPreferenceDto) {
        return ResponseEntity.ok(searchPreferenceService.updateSearchPreferences(searchPreferenceDto));
    }
}