package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AssignmentRequestDto(
    @NotBlank String title,
    String description,
    @NotNull Long lessonId,
    @FutureOrPresent LocalDateTime dueAt,
    Integer maxScore) {}
