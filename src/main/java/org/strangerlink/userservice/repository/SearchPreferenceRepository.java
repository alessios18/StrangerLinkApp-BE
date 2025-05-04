package org.strangerlink.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.userservice.model.SearchPreference;
import java.util.Optional;

public interface SearchPreferenceRepository extends JpaRepository<SearchPreference, Long> {
    Optional<SearchPreference> findByUserId(Long userId);
}
