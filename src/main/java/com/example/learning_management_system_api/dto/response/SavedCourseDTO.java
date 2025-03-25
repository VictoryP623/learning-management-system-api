package com.example.learning_management_system_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SavedCourseDTO {
  private Long courseId;
  private String courseName;
}
