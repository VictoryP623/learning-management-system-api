package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Lesson;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LessonMapper {

  // Request.resourceUrl -> Entity.videoUrl
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "course", ignore = true)
  @Mapping(target = "videoUrl", source = "resourceUrl")
  Lesson toEntity(LessonRequestDto dto);

  // Entity.videoUrl -> Response.resourceUrl
  @Mapping(
      target = "courseId",
      expression = "java(entity.getCourse() != null ? entity.getCourse().getId() : null)")
  @Mapping(
      target = "courseName",
      expression = "java(entity.getCourse() != null ? entity.getCourse().getName() : null)")
  @Mapping(target = "resourceUrl", source = "videoUrl")
  LessonResponseDto toDto(Lesson entity);

  // Patch/update: map resourceUrl -> videoUrl, và durationSec
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "course", ignore = true)
  @Mapping(target = "videoUrl", source = "resourceUrl")
  void updateLessonEntity(LessonRequestDto dto, @MappingTarget Lesson entity);
}
