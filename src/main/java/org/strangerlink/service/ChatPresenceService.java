// src/main/java/org/strangerlink/chatservice/service/ChatPresenceService.java
package org.strangerlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.strangerlink.repository.ConversationRepository;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatPresenceService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String TYPING_USERS_KEY = "typing_users";
    private static final int ONLINE_EXPIRY_SECONDS = 60; // User considered offline after 60s without heartbeat
    private static final int TYPING_EXPIRY_SECONDS = 10; // Typing indicator expires after 10s

    /**
     * Updates user's online status with an expiry
     * @param userId User ID
     */
    public void updateUserPresence(Long userId) {
        String key = ONLINE_USERS_KEY + ":" + userId;
        redisTemplate.opsForValue().set(key, System.currentTimeMillis());
        redisTemplate.expire(key, ONLINE_EXPIRY_SECONDS, TimeUnit.SECONDS);

        // No need to notify others here - will be handled by scheduled task
    }

    /**
     * Checks if a user is currently online
     * @param userId User ID
     * @return true if user is online
     */
    public boolean isUserOnline(Long userId) {
        String key = ONLINE_USERS_KEY + ":" + userId;
        return redisTemplate.hasKey(key);
    }

    /**
     * Sets a user's typing status in a conversation
     * @param userId User ID
     * @param conversationId Conversation ID
     * @param isTyping true if user is typing, false to clear typing indicator
     */
    public void setUserTypingStatus(Long userId, Long conversationId, boolean isTyping) {
        String key = TYPING_USERS_KEY + ":" + conversationId + ":" + userId;

        if (isTyping) {
            redisTemplate.opsForValue().set(key, System.currentTimeMillis());
            redisTemplate.expire(key, TYPING_EXPIRY_SECONDS, TimeUnit.SECONDS);

            // Notify the other user in conversation
            conversationRepository.findById(conversationId).ifPresent(conversation -> {
                Long otherUserId = conversation.getUser1Id().equals(userId)
                        ? conversation.getUser2Id() : conversation.getUser1Id();

                messagingTemplate.convertAndSendToUser(
                        otherUserId.toString(),
                        "/queue/typing",
                        new TypingIndicator(conversationId, userId, true)
                );
            });
        } else {
            redisTemplate.delete(key);

            // Notify typing stopped
            conversationRepository.findById(conversationId).ifPresent(conversation -> {
                Long otherUserId = conversation.getUser1Id().equals(userId)
                        ? conversation.getUser2Id() : conversation.getUser1Id();

                messagingTemplate.convertAndSendToUser(
                        otherUserId.toString(),
                        "/queue/typing",
                        new TypingIndicator(conversationId, userId, false)
                );
            });
        }
    }

    /**
     * Checks if a user is typing in a conversation
     * @param userId User ID
     * @param conversationId Conversation ID
     * @return true if user is typing
     */
    public boolean isUserTyping(Long userId, Long conversationId) {
        String key = TYPING_USERS_KEY + ":" + conversationId + ":" + userId;
        return redisTemplate.hasKey(key);
    }

    /**
     * Returns all users currently typing in a conversation
     * @param conversationId Conversation ID
     * @return Set of user IDs who are typing
     */
    public Set<String> getUsersTypingInConversation(Long conversationId) {
        String pattern = TYPING_USERS_KEY + ":" + conversationId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        // Extract user IDs from keys
        Set<String> userIds = new java.util.HashSet<>();
        if (keys != null) {
            for (String key : keys) {
                String[] parts = key.split(":");
                if (parts.length == 3) {
                    userIds.add(parts[2]);
                }
            }
        }

        return userIds;
    }

    /**
     * Record when a user goes offline explicitly (e.g., on logout)
     * @param userId User ID
     */
    public void setUserOffline(Long userId) {
        String key = ONLINE_USERS_KEY + ":" + userId;
        redisTemplate.delete(key);

        // Notify all conversations this user is in
        conversationRepository.findConversationsByUserId(userId).forEach(conversation -> {
            Long otherUserId = conversation.getUser1Id().equals(userId)
                    ? conversation.getUser2Id() : conversation.getUser1Id();

            messagingTemplate.convertAndSendToUser(
                    otherUserId.toString(),
                    "/queue/user-status",
                    new UserStatusUpdate(userId, false)
            );
        });
    }

    /**
     * POJO for typing indicator notifications
     */
    private record TypingIndicator(Long conversationId, Long userId, boolean typing) {}

    /**
     * POJO for user status updates
     */
    private record UserStatusUpdate(Long userId, boolean online) {}
}