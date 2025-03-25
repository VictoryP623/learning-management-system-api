package com.example.learning_management_system_api.utils.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
  Admin,
  Instructor,
  Student;

  @JsonValue
  public String getRole() {
    return this.name(); // or customize the return value if needed
  }
}
