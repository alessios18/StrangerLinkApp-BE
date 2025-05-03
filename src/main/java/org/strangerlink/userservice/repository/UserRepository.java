package org.strangerlink.userservice.repository;

import org.strangerlink.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:age IS NULL OR u.profile.age = :age) AND " +
            "(:country IS NULL OR u.profile.country = :country) AND " +
            "(:gender IS NULL OR u.profile.gender = :gender) AND " +
            "u.id NOT IN (SELECT bu.id FROM User blocker JOIN blocker.blockedUsers bu WHERE blocker.id = :userId) AND " +
            "u.id <> :userId")
    List<User> findMatchingUsers(
            @Param("age") Integer age,
            @Param("country") String country,
            @Param("gender") String gender,
            @Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.lastActive > :timestamp")
    List<User> findOnlineUsers(@Param("timestamp") java.time.LocalDateTime timestamp);
}