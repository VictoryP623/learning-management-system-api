package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;

public interface ICourseService {
  PageDto getAllCourse(
      int page,
      int limit,
      String courseName,
      String categoryName,
      Double price,
      Long instructorId,
      CustomUserDetails userDetails);

  CourseResponseDto getCourse(Long id);

  PageDto getStudentOfCourse(Long id, int page, int limit);

  PageDto getReviewOfCourse(Long id, int page, int limit);

  CourseResponseDto updateCourseStatus(Long courseId, String status);
}