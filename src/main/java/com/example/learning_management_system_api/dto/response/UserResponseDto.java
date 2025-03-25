package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.utils.enums.UserRole;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserResponseDto {

  private Long id;
  private String email;
  private String fullname;
  private UserRole role;
  private LocalDateTime birthdate;
  private String address;
  private UserStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
