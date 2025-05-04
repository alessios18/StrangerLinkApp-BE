package org.strangerlink.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.userservice.model.Country;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
}