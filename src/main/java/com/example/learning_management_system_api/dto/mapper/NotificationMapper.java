package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

  public NotificationResponse toDto(Notification n) {
    return NotificationResponse.builder()
        .id(n.getId())
        .recipientId(n.getRecipient() != null ? n.getRecipient().getId() : null)
        .actorId(n.getActor() != null ? n.getActor().getId() : null)
        .type(n.getType())
        .title(n.getTitle())
        .message(n.getMessage())
        .linkUrl(n.getLinkUrl())
        .readFlag(n.isReadFlag())
        .createdAt(n.getCreatedAt())
        .build();
  }
}
