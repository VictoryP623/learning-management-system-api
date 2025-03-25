package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.utils.enums.UserRole;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
  private String email;

  private String password;

  private String fullname;

  private UserRole role;

  private String refreshToken;

  private UserStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}
