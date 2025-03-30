package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;

import java.util.List;

public interface ILessonService {
    LessonResponseDto createLesson(LessonRequestDto requestDTO);

    LessonResponseDto getLessonById(Long id);

    List<LessonResponseDto> getAllLessons(Long courseId, String name);

    LessonResponseDto updateLesson(Long id, LessonRequestDto requestDTO);

    void deleteLesson(Long id);
}
