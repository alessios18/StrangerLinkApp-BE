package org.strangerlink.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.strangerlink.model.User;
import org.strangerlink.repository.UserRepository;
import org.strangerlink.service.ChatService;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue","/user");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Be cautious with this in production
                .withSockJS();

        // Also add a direct WebSocket endpoint for clients that don't need SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                // Check for SUBSCRIBE event to specific destination
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/user/") &&
                            destination.endsWith("/queue/messages")) {

                        // Extract user ID and conversation ID from destination
                        Principal user = accessor.getUser();
                        if (user != null) {
                            Long userId = getPrincipalUserId(user);

                            // Extract conversation ID if present in destination
                            // Format: /user/{userId}/queue/messages/{conversationId}
                            String[] parts = destination.split("/");
                            if (parts.length > 4) {
                                try {
                                    Long conversationId = Long.parseLong(parts[4]);
                                    // Mark messages as delivered
                                    chatService.markMessagesAsDelivered(conversationId, userId);
                                } catch (NumberFormatException e) {
                                    // Invalid conversation ID format
                                }
                            }
                        }
                    }
                }
                return message;
            }
        });
    }

    private Long getPrincipalUserId(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}