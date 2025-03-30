package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.request.CourseRequestDto;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.service.CourseService;
import com.example.learning_management_system_api.service.ICourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

  private final CourseMapper courseMapper;
  private final ICourseService iCourseService;

  public CourseController(
      CourseService courseService, CourseMapper courseMapper, ICourseService iCourseService) {
    this.courseMapper = courseMapper;
    this.iCourseService = iCourseService;
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getAllCourses(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Double price,
      @RequestParam(required = false) String categoryName,
      @RequestParam(required = false) String courseName,
      @RequestParam(required = false) Long instructorId) {
    PageDto result =
        iCourseService.getAllCourse(page, limit, courseName, categoryName, price, instructorId);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<CourseResponseDto> getCourse(@PathVariable Long id) {
    return new ResponseEntity<>(iCourseService.getCourse(id), HttpStatus.OK);
  }

  @GetMapping("/{id}/students")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getStudentsOfCourse(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return new ResponseEntity<>(iCourseService.getStudentOfCourse(id, page, limit), HttpStatus.OK);
  }

  @GetMapping("/{id}/reviews")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getReviewsOfCourse(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return new ResponseEntity<>(iCourseService.getReviewOfCourse(id, page, limit), HttpStatus.OK);
  }

  @PatchMapping("/{courseId}/status")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseEntity<CourseResponseDto> updateCourseStatus(
      @PathVariable Long courseId, @RequestBody CourseRequestDto request) {
    CourseResponseDto response = iCourseService.updateCourseStatus(courseId, request.status());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
