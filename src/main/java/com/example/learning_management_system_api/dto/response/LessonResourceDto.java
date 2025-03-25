package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

public record LessonResourceDto(
    Long id,
    String url,
    String name,
    Long lessonId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
