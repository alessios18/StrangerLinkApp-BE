package org.strangerlink.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.dto.InterestDto;
import org.strangerlink.model.Interest;
import org.strangerlink.repository.InterestRepository;

import java.util.List;

@RestController
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestRepository interestRepository;

    public InterestController(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        return ResponseEntity.ok(interestRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Interest> createInterest(@RequestBody @Valid InterestDto interestDto) {
        if (interestRepository.existsByName(interestDto.getName())) {
            return ResponseEntity.badRequest().build();
        }

        Interest interest = new Interest();
        interest.setName(interestDto.getName());

        return ResponseEntity.ok(interestRepository.save(interest));
    }
}