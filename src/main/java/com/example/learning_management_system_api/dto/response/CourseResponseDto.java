package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponseDto(
    Long id,
    Long instructorUserId, 
    String instructorName,
    String categoryName,
    Double price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String thumbnail,
    String status,
    String name,
    Long categoryId,
    String rejectedReason,
    List<LessonResponseDto> lessons,
    Integer completedLessons,
    Integer totalLessons,
    Double rating) {}
