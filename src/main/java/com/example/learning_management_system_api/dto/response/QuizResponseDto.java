package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.entity.AnswerOption;
import com.example.learning_management_system_api.utils.enums.QuizType;
import java.util.List;

public record QuizResponseDto(
    Long id, String question, QuizType quizType, Long lessonId, List<AnswerOption> answerOptions) {}
