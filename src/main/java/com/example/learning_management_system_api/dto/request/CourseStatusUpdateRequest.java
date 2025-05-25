package com.example.learning_management_system_api.dto.request;

import lombok.Data;

@Data
public class CourseStatusUpdateRequest {
  private String status;
  private String rejectedReason;
}