// Conversation Model
package org.strangerlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long user1Id;

    @Column(nullable = false)
    private Long user2Id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    @Column(columnDefinition = "TEXT")
    private String lastMessagePreview;

    // Count of unread messages for user1
    private Integer unreadCountUser1 = 0;

    // Count of unread messages for user2
    private Integer unreadCountUser2 = 0;
}