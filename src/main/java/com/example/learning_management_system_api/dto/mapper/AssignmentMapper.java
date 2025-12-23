package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.request.AssignmentRequestDto;
import com.example.learning_management_system_api.dto.response.AssignmentResponseDto;
import com.example.learning_management_system_api.entity.Assignment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentMapper {

  @Mapping(source = "lesson.id", target = "lessonId")
  @Mapping(source = "lesson.name", target = "lessonName")
  AssignmentResponseDto toDto(Assignment assignment);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(AssignmentRequestDto dto, @MappingTarget Assignment entity);
}
