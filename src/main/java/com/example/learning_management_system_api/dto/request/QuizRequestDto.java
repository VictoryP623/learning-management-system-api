package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.entity.AnswerOption;
import com.example.learning_management_system_api.utils.enums.QuizType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizRequestDto(
    @NotBlank String question,
    @NotNull QuizType quizType,
    @NotNull Long lessonId,
    @Size(min = 2, message = "Must have at least two answers or more") @Valid
        List<AnswerOption> answerOptions) {}
