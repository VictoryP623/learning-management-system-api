package com.example.learning_management_system_api.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

public record QuizAnswerDto(@NotNull Long quizId, @NotNull List<Integer> answerIds) {}
