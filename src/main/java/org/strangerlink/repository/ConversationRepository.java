package org.strangerlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.strangerlink.model.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findConversationsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE (c.user1Id = :user1Id AND c.user2Id = :user2Id) OR " +
            "(c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    Optional<Conversation> findConversationBetweenUsers(
            @Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
}
