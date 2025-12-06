package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LessonResponseDto {
  private Long id;
  private String name;
  private String description;
  private Long courseId;
  private String courseName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean isFree;
  private String resourceUrl; // videoUrl hoặc preview
  private Boolean completed;
  private Integer orderIndex;
  private Boolean locked;
  private Long nextLessonId;
  private Boolean nextLessonLocked;
  private String nextLessonName;

  public boolean isCompleted() {
    return completed != null && completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }
}
