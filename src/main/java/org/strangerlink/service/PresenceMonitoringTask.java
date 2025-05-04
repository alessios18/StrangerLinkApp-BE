// src/main/java/org/strangerlink/chatservice/service/PresenceMonitoringTask.java
package org.strangerlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.strangerlink.repository.ConversationRepository;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PresenceMonitoringTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    private static final String ONLINE_USERS_KEY = "online_users";
    private Set<Long> previouslyOnlineUsers = new HashSet<>();

    /**
     * Runs every 15 seconds to check for users who have gone offline by timeout
     */
    @Scheduled(fixedRate = 15000)
    public void checkOfflineUsers() {
        Set<String> currentOnlineKeys = redisTemplate.keys(ONLINE_USERS_KEY + ":*");
        Set<Long> currentOnlineUsers = new HashSet<>();

        // Extract user IDs from keys
        if (currentOnlineKeys != null) {
            for (String key : currentOnlineKeys) {
                String[] parts = key.split(":");
                if (parts.length == 2) {
                    try {
                        currentOnlineUsers.add(Long.parseLong(parts[1]));
                    } catch (NumberFormatException e) {
                        // Ignore malformed keys
                    }
                }
            }
        }

        // Find users who were online before but are now offline
        Set<Long> newlyOfflineUsers = new HashSet<>(previouslyOnlineUsers);
        newlyOfflineUsers.removeAll(currentOnlineUsers);

        // Notify for each user who has gone offline
        for (Long userId : newlyOfflineUsers) {
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

        // Find users who were offline before but are now online
        Set<Long> newlyOnlineUsers = new HashSet<>(currentOnlineUsers);
        newlyOnlineUsers.removeAll(previouslyOnlineUsers);

        // Notify for each user who has come online
        for (Long userId : newlyOnlineUsers) {
            conversationRepository.findConversationsByUserId(userId).forEach(conversation -> {
                Long otherUserId = conversation.getUser1Id().equals(userId)
                        ? conversation.getUser2Id() : conversation.getUser1Id();

                messagingTemplate.convertAndSendToUser(
                        otherUserId.toString(),
                        "/queue/user-status",
                        new UserStatusUpdate(userId, true)
                );
            });
        }

        // Update previous state for next comparison
        previouslyOnlineUsers = currentOnlineUsers;
    }

    /**
     * POJO for user status updates
     */
    private record UserStatusUpdate(Long userId, boolean online) {}
}