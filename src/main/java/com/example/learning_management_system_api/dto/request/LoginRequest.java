package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
  @NotBlank(message = "Email là bắt buộc")
  private String email;

  @NotBlank(message = "Password là bắt buộc")
  private String password;
}
