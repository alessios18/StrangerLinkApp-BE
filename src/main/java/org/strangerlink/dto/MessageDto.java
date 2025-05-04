package org.strangerlink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private Long conversationId;
    private String content;
    private long timestamp;
    private String type;
    private String status;
    private String mediaUrl;
    private String mediaType;
    private Long receiverId;
}