package org.strangerlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private Long id;
    private UserDto otherUser;
    private String lastMessage;
    private long lastMessageTimestamp;
    private int unreadCount;
    private boolean isOnline;
}