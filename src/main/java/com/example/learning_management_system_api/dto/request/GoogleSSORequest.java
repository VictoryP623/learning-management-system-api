package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GoogleSSORequest {
  @Pattern(
      regexp = "^Student|Instructor$",
      message = "Role phải là một trong {Student, Instructor}")
  private String role;

  @NotBlank(message = "Code là bắt buộc")
  private String code;

  public GoogleSSORequest(String googleAuthCode, String userRole) {
    this.role = userRole;
    this.code = googleAuthCode;
  }
}