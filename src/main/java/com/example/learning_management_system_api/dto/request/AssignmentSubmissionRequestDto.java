package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignmentSubmissionRequestDto(
    @NotNull Long assignmentId, String textAnswer, String attachmentUrl) {}
