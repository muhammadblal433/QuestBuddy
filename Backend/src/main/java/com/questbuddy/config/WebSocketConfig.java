package com.questbuddy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // FE & Postman
        // .withSockJS(); // enable if FE uses SockJS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // App -> Server destinations would be under /app (not used for notifications)
        registry.setApplicationDestinationPrefixes("/app");
        // Server -> Client topics (simple in-memory broker) under /topic
        registry.enableSimpleBroker("/topic");
    }
}