package com.example.learning_management_system_api.config;

import com.example.learning_management_system_api.component.JwtUtils;
import java.security.Principal;
import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WsAuthChannelInterceptor implements ChannelInterceptor {

  private final JwtUtils jwtUtils;

  public WsAuthChannelInterceptor(JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = resolveToken(accessor);

      if (token != null) {
        try {
          if (jwtUtils.validateToken(token)) {
            Long userId = jwtUtils.getUserIdFromToken(token);

            Principal principal = () -> String.valueOf(userId);
            accessor.setUser(principal);

            System.out.println("[WS] CONNECT ok, principal=" + principal.getName());
          } else {
            System.out.println("[WS] CONNECT invalid token");
          }
        } catch (Exception e) {
          System.out.println("[WS] CONNECT token parse error: " + e.getMessage());
        }
      } else {
        System.out.println("[WS] CONNECT no token header/query");
      }
    }

    return message;
  }

  private String resolveToken(StompHeaderAccessor accessor) {
    // 1) Native headers
    String auth = firstNativeHeader(accessor, "Authorization");
    if (auth == null) auth = firstNativeHeader(accessor, "authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      return auth.substring(7);
    }

    // 2) Query param fallback: /ws?token=...
    if (accessor.getSessionAttributes() != null) {
      Object raw = accessor.getSessionAttributes().get("token");
      if (raw instanceof String s && !s.isBlank()) return s;
    }

    return null;
  }

  private String firstNativeHeader(StompHeaderAccessor accessor, String key) {
    List<String> values = accessor.getNativeHeader(key);
    if (values == null || values.isEmpty()) return null;
    return values.get(0);
  }
}
