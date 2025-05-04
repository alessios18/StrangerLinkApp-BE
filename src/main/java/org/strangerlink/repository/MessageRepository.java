// Message Repository
package org.strangerlink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.strangerlink.model.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdOrderByTimestampDesc(Long conversationId, Pageable pageable);

    List<Message> findByConversationIdAndTimestampAfterOrderByTimestamp(
            Long conversationId, LocalDateTime timestamp);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId " +
            "AND m.status = 'DELIVERED' AND m.senderId <> :userId")
    List<Message> findUnreadMessages(@Param("conversationId") Long conversationId,
                                     @Param("userId") Long userId);
}