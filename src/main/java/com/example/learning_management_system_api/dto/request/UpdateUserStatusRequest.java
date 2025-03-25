package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.utils.enums.UserStatus;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
  private UserStatus status;
}
