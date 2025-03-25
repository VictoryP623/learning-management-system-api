package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.NotNull;

public record QuizAnswerDto(@NotNull Long quizId, Integer answerId) {}
