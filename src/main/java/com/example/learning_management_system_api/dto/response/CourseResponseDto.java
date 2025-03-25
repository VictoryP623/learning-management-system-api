package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.entity.Course;
import java.time.LocalDateTime;

public record CourseResponseDto(
    Long id,
    String instructorName,
    String categoryName,
    Double price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String thumbnail,
    String status,
    String name,
    Long categoryId) {
  public CourseResponseDto(Course course) {
    this(
        course.getId(),
        course.getInstructor().getUser().getFullname(),
        course.getCategory().getName(),
        course.getPrice(),
        course.getCreatedAt(),
        course.getUpdatedAt(),
        course.getThumbnail(),
        course.getStatus(),
        course.getName(),
        course.getCategory().getId());
  }
}
