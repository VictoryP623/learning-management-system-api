package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.utils.enums.NotificationTopic;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {
  private Long id;
  private Long recipientId;
  private Long actorId;
  private NotificationType type;
  private NotificationTopic topic;

  private String title;
  private String message;
  private String linkUrl;

  private String dataJson;

  private boolean readFlag;
  private LocalDateTime createdAt;
}
