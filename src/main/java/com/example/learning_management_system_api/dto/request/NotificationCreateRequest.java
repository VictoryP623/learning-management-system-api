package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.utils.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationCreateRequest {
  /** Bắt buộc: id người nhận (recipient) */
  @NotNull private Long recipientId;

  /** Tuỳ chọn: id người gây ra hành động (actor) */
  private Long actorId;

  @NotNull private NotificationType type; // INFO/SUCCESS/WARNING/ERROR

  @NotBlank private String title;

  @NotBlank private String message;

  /** Tuỳ chọn deep-link FE (vd: /courses/12/assignments/3) */
  private String linkUrl;
}