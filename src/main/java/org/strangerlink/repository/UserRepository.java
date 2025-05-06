package org.strangerlink.repository;

import org.strangerlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


    @Query("SELECT u FROM User u WHERE u.lastActive > :timestamp")
    List<User> findOnlineUsers(@Param("timestamp") java.time.LocalDateTime timestamp);

    @Query("SELECT u FROM User u JOIN u.profile p WHERE " +
            "(:minAge IS NULL OR p.age >= :minAge) AND " +
            "(:maxAge IS NULL OR p.age <= :maxAge) AND " +
            "(:gender IS NULL OR p.gender = :gender) AND " +
            "(:allCountries = true OR (:countryId IS NULL OR p.country.id = :countryId)) AND " +
            "u.id NOT IN (SELECT bu.id FROM User blocker JOIN blocker.blockedUsers bu WHERE blocker.id = :userId) AND " +
            "u.id <> :userId")
    List<User> findMatchingUsers(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") String gender,
            @Param("countryId") Long countryId,
            @Param("allCountries") boolean allCountries,
            @Param("userId") Long userId);

    @Query("SELECT u FROM User u JOIN u.profile p WHERE " +
            "(:minAge IS NULL OR p.age >= :minAge) AND " +
            "(:maxAge IS NULL OR p.age <= :maxAge) AND " +
            "(:gender IS NULL OR p.gender = :gender) AND " +
            "(:allCountries = true OR (:countryId IS NULL OR p.country.id = :countryId)) AND " +
            "u.id NOT IN (SELECT c.user1Id FROM Conversation c WHERE c.user2Id = :userId) AND " +
            "u.id NOT IN (SELECT c.user2Id FROM Conversation c WHERE c.user1Id = :userId) AND " +
            "u.id NOT IN (SELECT bu.id FROM User blocker JOIN blocker.blockedUsers bu WHERE blocker.id = :userId) AND " +
            "u.id <> :userId " +
            "ORDER BY u.lastActive DESC")
    List<User> findRandomMatchingUser(
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            @Param("gender") String gender,
            @Param("countryId") Long countryId,
            @Param("allCountries") boolean allCountries,
            @Param("userId") Long userId);
}