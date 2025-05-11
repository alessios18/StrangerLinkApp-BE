// Chat Service
package org.strangerlink.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.strangerlink.dto.ConversationDto;
import org.strangerlink.dto.MessageDto;
import org.strangerlink.model.Conversation;
import org.strangerlink.model.Message;
import org.strangerlink.repository.ConversationRepository;
import org.strangerlink.repository.MessageRepository;
import org.strangerlink.dto.UserDto;
import org.strangerlink.model.User;
import org.strangerlink.repository.UserRepository;
import org.strangerlink.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String ONLINE_USERS_KEY = "online_users";

    @Transactional
    public MessageDto sendMessage(Long senderId, Long receiverId, MessageDto messageDto) {
        // Get or create conversation
        Conversation conversation;
        boolean isNewConversation = false;

        if (messageDto.getConversationId() == null || messageDto.getConversationId() == 0) {
            // Create new conversation
            conversation = new Conversation();
            conversation.setUser1Id(senderId);
            conversation.setUser2Id(receiverId);
            conversation.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
            conversation = conversationRepository.save(conversation);
            isNewConversation = true;
        } else {
            // Get existing conversation
            conversation = conversationRepository.findById(messageDto.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        }

        // Create message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setConversationId(conversation.getId());
        message.setContent(messageDto.getContent());
        message.setTimestamp(DateTimeUtils.fromEpocToDateTime(messageDto.getTimestamp()));
        message.setType(Message.MessageType.valueOf(messageDto.getType()));
        message.setStatus(Message.MessageStatus.SENT);
        message.setMediaUrl(messageDto.getMediaUrl());
        message.setMediaType(messageDto.getMediaType());

        Message savedMessage = messageRepository.save(message);

        // Update conversation
        conversation.setLastMessageAt(message.getTimestamp());
        conversation.setLastMessagePreview(message.getContent());

        // Update unread count for the receiver
        if (conversation.getUser1Id().equals(receiverId)) {
            conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
        } else {
            conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
        }

        conversationRepository.save(conversation);

        // Convert to DTO
        MessageDto savedMessageDto = convertToMessageDto(savedMessage);

        // Send message to WebSocket
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                savedMessageDto
        );

        // If this is a new conversation, notify the receiver about the new conversation
        if (isNewConversation) {
            messagingTemplate.convertAndSendToUser(
                    receiverId.toString(),
                    "/queue/new-conversation",
                    true
            );
        }

        return savedMessageDto;
    }

    @Transactional
    public void markMessagesAsDelivered(Long conversationId, Long userId) {
        // Find messages that are SENT but not yet DELIVERED
        List<Message> sentMessages = messageRepository.findMessagesByStatusAndRecipient(
                conversationId, Message.MessageStatus.SENT, userId);

        if (!sentMessages.isEmpty()) {
            // Log activity for debugging
            System.out.println("Marking " + sentMessages.size() + " messages as DELIVERED for user " + userId);

            // Update to DELIVERED
            sentMessages.forEach(message -> message.setStatus(Message.MessageStatus.DELIVERED));
            List<Message> updatedMessages = messageRepository.saveAll(sentMessages);

            // Notify senders about delivery status updates
            updatedMessages.forEach(message -> {
                MessageDto messageDto = convertToMessageDto(message);
                System.out.println("Sending delivery notification to user " + message.getSenderId() + " for message " + message.getId());
                messagingTemplate.convertAndSendToUser(
                        message.getSenderId().toString(),
                        "/queue/message-status",
                        messageDto
                );
            });
        }
    }

    @Transactional
    public List<MessageDto> getMessages(Long conversationId, Long userId, int page, int size) {
        return messageRepository.findByConversationIdOrderByTimestampDesc(
                        conversationId,
                        PageRequest.of(page, size, Sort.by("timestamp").descending())
                )
                .getContent()
                .stream()
                .map(this::convertToMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ConversationDto> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> {
                    Long otherUserId = conversation.getUser1Id().equals(userId)
                            ? conversation.getUser2Id() : conversation.getUser1Id();

                    User otherUser = userRepository.findById(otherUserId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    UserDto otherUserDto = UserDto.builder()
                            .id(otherUser.getId())
                            .username(otherUser.getUsername())
                            .email(otherUser.getEmail())
                            .build();

                    int unreadCount = conversation.getUser1Id().equals(userId)
                            ? conversation.getUnreadCountUser1()
                            : conversation.getUnreadCountUser2();

                    boolean isOnline = isUserOnline(otherUserId);

                    return ConversationDto.builder()
                            .id(conversation.getId())
                            .otherUser(otherUserDto)
                            .lastMessage(conversation.getLastMessagePreview())
                            .lastMessageTimestamp(conversation.getLastMessageAt() != null
                                    ? conversation.getLastMessageAt().toInstant(ZoneOffset.UTC).toEpochMilli()
                                    : 0)
                            .unreadCount(unreadCount)
                            .isOnline(isOnline)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(Long conversationId, Long userId) {
        // Log per debug
        System.out.println("Marking messages as READ in conversation " + conversationId + " for user " + userId);

        List<Message> unreadMessages = messageRepository.findUnreadMessages(conversationId, userId);

        if (!unreadMessages.isEmpty()) {
            System.out.println("Found " + unreadMessages.size() + " messages to mark as READ");

            unreadMessages.forEach(message -> {
                message.setStatus(Message.MessageStatus.READ);
                System.out.println("Marked message " + message.getId() + " as READ");
            });

            messageRepository.saveAll(unreadMessages);

            // Notifica esplicita ai mittenti
            unreadMessages.forEach(message -> {
                MessageDto messageDto = convertToMessageDto(message);
                System.out.println("Sending READ notification to user " + message.getSenderId() + " for message " + message.getId());
                messagingTemplate.convertAndSendToUser(
                        message.getSenderId().toString(),
                        "/queue/message-status",
                        messageDto
                );
            });
        }
    }

    public void setUserOnlineStatus(Long userId, boolean isOnline) {
        String key = ONLINE_USERS_KEY + ":" + userId;
        if (isOnline) {
            redisTemplate.opsForValue().set(key, "online");
        } else {
            redisTemplate.delete(key);
        }

        // Notify friends/contacts about status change
        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);
        conversations.forEach(conversation -> {
            Long otherUserId = conversation.getUser1Id().equals(userId)
                    ? conversation.getUser2Id() : conversation.getUser1Id();

            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/user-status",
                    new UserStatusDto(userId, isOnline)
            );
        });
    }

    public boolean isUserOnline(Long userId) {
        String key = ONLINE_USERS_KEY + ":" + userId;
        return redisTemplate.hasKey(key);
    }

    private Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        Optional<Conversation> existingConversation =
                conversationRepository.findConversationBetweenUsers(user1Id, user2Id);

        if (existingConversation.isPresent()) {
            return existingConversation.get();
        }

        Conversation newConversation = new Conversation();
        newConversation.setUser1Id(user1Id);
        newConversation.setUser2Id(user2Id);
        newConversation.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        return conversationRepository.save(newConversation);
    }

    private MessageDto convertToMessageDto(Message message) {
        return MessageDto.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .conversationId(message.getConversationId())
                .content(message.getContent())
                .timestamp(message.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli())
                .type(message.getType().name())
                .status(message.getStatus().name())
                .mediaUrl(message.getMediaUrl())
                .mediaType(message.getMediaType())
                .build();
    }

    @Data
    @AllArgsConstructor
    private static class UserStatusDto {
        private Long userId;
        private boolean online;
    }
}