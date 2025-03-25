package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.QuizAttemptResponseDto;
import com.example.learning_management_system_api.entity.QuizAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface QuizAttemptMapper {

  @Mappings({
    @Mapping(source = "quiz.id", target = "quizId"),
    @Mapping(source = "user.id", target = "userId")
  })
  QuizAttemptResponseDto toDto(QuizAttempt quizAttempt);
}
