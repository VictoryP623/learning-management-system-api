package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;

public record QuizAttemptResponseDto(
    Long quizId,
    Long userId,
    Integer answerId,
    boolean isCorrect,
    LocalDateTime attemptTimestamp) {}
