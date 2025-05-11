// Chat Controller
package org.strangerlink.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.strangerlink.dto.ConversationDto;
import org.strangerlink.dto.MessageDto;
import org.strangerlink.service.ChatPresenceService;
import org.strangerlink.service.ChatService;
import org.strangerlink.model.User;
import org.strangerlink.repository.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatPresenceService chatPresenceService;
    private final UserRepository userRepository;

    // ... existing endpoints

    @MessageMapping("/chat.presence")
    public void updatePresence(Principal principal) {

        chatPresenceService.updateUserPresence(getPrincipalUserId(principal));
    }

    @MessageMapping("/chat.typing")
    public void processTypingIndicator(@Payload TypingDto typingDto, Principal principal) {
        chatPresenceService.setUserTypingStatus(getPrincipalUserId(principal), typingDto.getConversationId(), typingDto.isTyping());
    }

    @GetMapping("/conversations/{conversationId}/typing")
    public ResponseEntity<Set<String>> getUsersTyping(@PathVariable Long conversationId) {
        return ResponseEntity.ok(chatPresenceService.getUsersTypingInConversation(conversationId));
    }

    @GetMapping("/users/{userId}/online")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable Long userId) {
        return ResponseEntity.ok(chatPresenceService.isUserOnline(userId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getUserConversations() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(chatService.getUserConversations(userId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(chatService.getMessages(conversationId, userId, page, size));
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long conversationId) {
        chatService.markMessagesAsRead(conversationId, getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @MessageMapping("/chat.delivered")
    public void markMessagesAsDelivered(@Payload DeliveryReceiptDto payload, Principal principal) {
        Long userId = getPrincipalUserId(principal);
        chatService.markMessagesAsDelivered(payload.getConversationId(), userId);
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long receiverId,
            @RequestBody MessageDto messageDto) {
        MessageDto sentMessage = chatService.sendMessage(getCurrentUserId(), receiverId, messageDto);
        return ResponseEntity.ok(sentMessage);
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload MessageDto messageDto, Principal principal) {
        chatService.sendMessage(getPrincipalUserId(principal), messageDto.getReceiverId(), messageDto);
    }

    private Long getPrincipalUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    @Data
    private static class TypingDto {
        private Long conversationId;
        private Long receiverId;
        private boolean typing;
    }

    @Data
    private static class DeliveryReceiptDto {
        private Long conversationId;
        private Long userId;
    }
}