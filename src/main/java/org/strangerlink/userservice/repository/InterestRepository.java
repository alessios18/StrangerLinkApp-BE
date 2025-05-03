package org.strangerlink.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.userservice.model.Interest;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByName(String name);
    boolean existsByName(String name);
}