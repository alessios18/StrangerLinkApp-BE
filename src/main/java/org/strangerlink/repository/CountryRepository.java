package org.strangerlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.model.Country;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCode(String code);
    Optional<Country> findByName(String name);
}