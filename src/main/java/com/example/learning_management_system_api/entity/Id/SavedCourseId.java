package com.example.learning_management_system_api.entity.Id;

import java.io.Serializable;
import lombok.Data;

@Data
public class SavedCourseId implements Serializable {
  private Long studentId;
  private Long courseId;
}