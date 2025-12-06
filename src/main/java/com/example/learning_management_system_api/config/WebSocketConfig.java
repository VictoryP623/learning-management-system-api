package com.example.learning_management_system_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns(
            "http://localhost:5173", "http://localhost:3000", "https://lms.example.com")
        .withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Phải có cả /topic và /queue để dùng convertAndSendToUser(..., "/queue/...", ...)
    registry.enableSimpleBroker("/topic", "/queue");

    // App prefix cho các message từ client gửi lên server (nếu bạn có controller @MessageMapping)
    registry.setApplicationDestinationPrefixes("/app");

    // QUAN TRỌNG: để Spring map /user/queue/... về đúng user
    registry.setUserDestinationPrefix("/user");
  }
}
