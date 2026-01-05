package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.utils.enums.NotificationTopic;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationCreateRequest {

  @NotNull private Long recipientId;
  private Long actorId;

  @NotNull private NotificationType type;

  @NotNull private NotificationTopic topic;

  /** optional: nếu null thì service tự sinh; nếu set thì chống gửi trùng */
  private String idempotencyKey;

  @NotBlank private String title;
  @NotBlank private String message;

  private String linkUrl;

  /** optional json payload */
  private String dataJson;
}
