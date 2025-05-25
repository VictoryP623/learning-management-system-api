package com.example.learning_management_system_api.dto.request;

import lombok.Data;

@Data
public class AdminRegisterRequest {
  private String email;
  private String password;
    private String fullname;
}
