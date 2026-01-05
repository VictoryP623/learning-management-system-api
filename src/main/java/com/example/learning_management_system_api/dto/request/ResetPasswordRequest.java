package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

  @NotBlank(message = "Password mới là bắt buộc")
  @Size(min = 6, message = "Vui lòng nhập ít nhất 6 ký tự")
  private String password;
}
