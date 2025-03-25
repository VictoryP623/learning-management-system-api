package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

  @Mapping(target = "instructorName", source = "instructor.user.fullname")
  @Mapping(source = "category.name", target = "categoryName")
  @Mapping(source = "category.id", target = "categoryId")
  CourseResponseDto toResponseDTO(Course course);
}
