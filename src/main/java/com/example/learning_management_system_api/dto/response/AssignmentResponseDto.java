package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AssignmentResponseDto {

  private Long id;
  private Long lessonId;
  private String lessonName;

  private String title;
  private String description;
  private LocalDateTime dueAt;
  private Integer maxScore;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
