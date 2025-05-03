package org.strangerlink.userservice.repository;

import org.strangerlink.userservice.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    @Query("SELECT p FROM Profile p WHERE " +
            "LOWER(p.interests) LIKE LOWER(CONCAT('%', :interest, '%'))")
    List<Profile> findByInterest(@Param("interest") String interest);

    @Query("SELECT p FROM Profile p WHERE " +
            "p.country = :country AND " +
            "p.age BETWEEN :minAge AND :maxAge")
    List<Profile> findByCountryAndAgeRange(
            @Param("country") String country,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge);

    @Query("SELECT p FROM Profile p JOIN p.user u WHERE " +
            "u.lastActive > :timestamp")
    List<Profile> findActiveProfiles(@Param("timestamp") java.time.LocalDateTime timestamp);
}