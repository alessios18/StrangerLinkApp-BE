package org.strangerlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.model.Interest;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByName(String name);
    boolean existsByName(String name);
}