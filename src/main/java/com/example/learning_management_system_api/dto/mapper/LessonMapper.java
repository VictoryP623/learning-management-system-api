package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Lesson;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LessonMapper {

  Lesson toEntity(LessonRequestDto lessonDto);

  @Mappings({
    @Mapping(source = "course.id", target = "courseId"),
    @Mapping(source = "course.name", target = "courseName"),
  })
  LessonResponseDto toDto(Lesson lesson);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateLessonEntity(LessonRequestDto sourceLesson, @MappingTarget Lesson targetLesson);
}
