package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/** DTO trả về cho FE (tránh lazy issues) */
@Data
@Builder
public class NotificationResponse {
  private Long id;
  private Long recipientId;
  private Long actorId; // có thể null
  private NotificationType type;
  private String title;
  private String message;
  private String linkUrl;
  private boolean readFlag;
  private LocalDateTime createdAt;
}