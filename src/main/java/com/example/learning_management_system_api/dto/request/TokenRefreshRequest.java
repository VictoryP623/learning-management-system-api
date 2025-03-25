package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshRequest {
  @NotBlank(message = "refreshToken là bắt buộc")
  String refreshToken;
}
