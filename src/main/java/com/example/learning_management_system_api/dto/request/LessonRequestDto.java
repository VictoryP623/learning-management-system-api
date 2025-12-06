package com.example.learning_management_system_api.dto.request;

import com.example.learning_management_system_api.utils.enums.LessonUnlockType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonRequestDto(
    @NotBlank String name,
    @NotBlank String description,
    @NotNull Long courseId,
    Boolean isFree,
    String resourceUrl, // video HLS chính
    Integer durationSec, // tổng thời lượng video (giây)
    LessonUnlockType unlockType, // NONE, PREVIOUS_COMPLETED, SPECIFIC_LESSON_COMPLETED
    Long requiredLessonId // nếu SPECIFIC_LESSON_COMPLETED thì điền id bài phải hoàn thành trước
    ) {}
