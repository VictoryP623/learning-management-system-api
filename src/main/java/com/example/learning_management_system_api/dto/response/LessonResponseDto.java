package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

public record LessonResponseDto(
    Long id,
    String name,
    String description,
    Long courseId,
    String courseName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
