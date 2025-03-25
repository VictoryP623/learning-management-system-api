package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.ReviewDTO;
import com.example.learning_management_system_api.dto.response.ReviewResponseDto;
import com.example.learning_management_system_api.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mappings({
    @Mapping(source = "course.id", target = "courseId"),
    @Mapping(source = "student.id", target = "studentId"),
  })
  ReviewResponseDto toResponseDTO(Review review);

  ReviewDTO toDto(Review review);

  Review toEntity(ReviewDTO review);
}