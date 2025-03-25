package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePasswordRequest {
  @NotBlank(message = "Password cũ là bắt buộc")
  private String oldPassword;

  @NotBlank(message = "Password mới là bắt buộc")
  private String newPassword;
}
