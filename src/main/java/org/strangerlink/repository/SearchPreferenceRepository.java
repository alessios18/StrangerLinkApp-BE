package org.strangerlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.strangerlink.model.SearchPreference;
import java.util.Optional;

public interface SearchPreferenceRepository extends JpaRepository<SearchPreference, Long> {
    Optional<SearchPreference> findByUserId(Long userId);
}
