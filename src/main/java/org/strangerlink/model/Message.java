// Chat Message Model
package org.strangerlink.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long conversationId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    // Link to media file if any
    private String mediaUrl;

    // Media type (if applicable)
    private String mediaType;

    @Column(nullable = true)
    private Long receiverId;

    public enum MessageType {
        TEXT, IMAGE, VIDEO, DOCUMENT
    }

    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
}