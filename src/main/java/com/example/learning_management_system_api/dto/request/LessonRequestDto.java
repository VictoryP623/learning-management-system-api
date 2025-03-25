package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonRequestDto(
    @NotBlank String name, @NotBlank String description, @NotNull Long courseId) {}
