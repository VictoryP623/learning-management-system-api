package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

public record ReviewResponseDto(
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long courseId,
    Long studentId) {}
