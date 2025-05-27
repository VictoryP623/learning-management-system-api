package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.request.CourseRequestDto;
import com.example.learning_management_system_api.dto.request.CourseStatusUpdateRequest;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.exception.NotFoundException;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.service.CourseService;
import com.example.learning_management_system_api.service.ICourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
// @CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

  private final CourseMapper courseMapper;
  private final ICourseService iCourseService;
  private final CourseService courseService;
  private final CourseRepository courseRepository;

  public CourseController(
      CourseService courseService,
      CourseMapper courseMapper,
      ICourseService iCourseService,
      CourseRepository courseRepository) {
    this.courseMapper = courseMapper;
    this.iCourseService = iCourseService;
    this.courseService = courseService;
    this.courseRepository = courseRepository;
  }

  @GetMapping("")
  //@PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<?> getCourses(
      @RequestParam int page,
      @RequestParam int limit,
      @RequestParam(required = false) String courseName,
      @RequestParam(required = false) String categoryName,
      @RequestParam(required = false) Double price,
      @RequestParam(required = false) Long instructorId,
      @AuthenticationPrincipal CustomUserDetails user) // Đúng kiểu này!
      {
    PageDto result =
        courseService.getAllCourse(
            page, limit, courseName, categoryName, price, instructorId, user);
    return ResponseEntity.ok(result);
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

  // @PatchMapping("/{courseId}/status")
  // @PreAuthorize("hasRole('ROLE_Admin')")
  // public ResponseEntity<CourseResponseDto> updateCourseStatus(
  //     @PathVariable Long courseId, @RequestBody CourseRequestDto request) {
  //   CourseResponseDto response = iCourseService.updateCourseStatus(courseId, request.status());
  //   return new ResponseEntity<>(response, HttpStatus.OK);
  // }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseEntity<?> updateCourseStatus(
      @PathVariable Long id, @RequestBody CourseStatusUpdateRequest request) {
    Course course =
        courseRepository.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    course.setStatus(request.getStatus());
    if ("REJECTED".equals(request.getStatus())) {
      course.setRejectedReason(request.getRejectedReason());
    } else {
      course.setRejectedReason(null);
    }
    courseRepository.save(course);
    return ResponseEntity.ok(courseMapper.toResponseDTO(course));
  }

  @PatchMapping("/{id}/resubmit")
  @PreAuthorize("hasRole('ROLE_Instructor')")
  public ResponseEntity<?> resubmitCourse(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
    Course course =
        courseRepository.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    if (!course.getInstructor().getUser().getId().equals(user.getUser().getId())) {
      throw new RuntimeException("You can't resubmit others' course.");
    }
    if (!"REJECTED".equals(course.getStatus())) {
      throw new IllegalArgumentException("Only rejected course can be resubmitted.");
    }
    course.setStatus("PENDING");
    course.setRejectedReason(null);
    courseRepository.save(course);
    return ResponseEntity.ok(courseMapper.toResponseDTO(course)); // Sử dụng MapStruct mapper
  }
}
