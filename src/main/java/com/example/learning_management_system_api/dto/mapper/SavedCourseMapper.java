package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.SavedCourseDTO;
import com.example.learning_management_system_api.entity.SavedCourse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SavedCourseMapper {

  @Mapping(source = "course.name", target = "courseName") // Map courseName from Course entity
  SavedCourseDTO savedCourseToSavedCourseDTO(SavedCourse savedCourse);
}
