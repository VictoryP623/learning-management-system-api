package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeSubmissionRequestDto(
    @NotNull Long submissionId, @Min(0) Integer score, String feedback) {}
