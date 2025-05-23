package com.example.learning_management_system_api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowResponseDto {
  private Long studentId;
  private Long instructorId;
  private String createdAt;
  private String studentName;
  private String instructorName;
}
