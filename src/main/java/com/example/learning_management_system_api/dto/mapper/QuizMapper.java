package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.QuizRequestDto;
import com.example.learning_management_system_api.dto.response.QuizResponseDto;
import com.example.learning_management_system_api.entity.Quiz;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface QuizMapper {

  Quiz toEntity(QuizRequestDto quizRequestDto);

  @Mappings({
    @Mapping(source = "lesson.id", target = "lessonId"),
  })
  QuizResponseDto toDto(Quiz quiz);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateQuiz(QuizRequestDto sourceQuiz, @MappingTarget Quiz targetQuiz);
}
