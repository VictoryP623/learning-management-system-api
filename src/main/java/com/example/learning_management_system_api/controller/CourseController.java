package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.request.CourseStatusUpdateRequest;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.StudentProgressDto;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.service.CourseService;
import com.example.learning_management_system_api.service.ICourseService;
import com.example.learning_management_system_api.service.InstructorCourseStudentService;
import java.util.List;
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
  private final InstructorCourseStudentService instructorCourseStudentService;

  public CourseController(
      CourseService courseService,
      CourseMapper courseMapper,
      ICourseService iCourseService,
      CourseRepository courseRepository,
      InstructorCourseStudentService instructorCourseStudentService) {
    this.courseMapper = courseMapper;
    this.iCourseService = iCourseService;
    this.courseService = courseService;
    this.courseRepository = courseRepository;
    this.instructorCourseStudentService = instructorCourseStudentService;
  }

  @GetMapping("")
  // @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<?> getCourses(
      @RequestParam int page,
      @RequestParam int limit,
      @RequestParam(required = false) String courseName,
      @RequestParam(required = false) String categoryName,
      @RequestParam(required = false) Double price,
      @RequestParam(required = false) Long instructorId,
      @AuthenticationPrincipal CustomUserDetails user 
      ) {
    PageDto result =
        courseService.getAllCourse(
            page, limit, courseName, categoryName, price, instructorId, user);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<CourseResponseDto> getCourse(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
    return new ResponseEntity<>(iCourseService.getCourse(id, user), HttpStatus.OK);
  }

  @GetMapping("/{id}/students")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getStudentsOfCourse(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return new ResponseEntity<>(iCourseService.getStudentOfCourse(id, page, limit), HttpStatus.OK);
  }

  @GetMapping("/{id}/students-progress")
  @PreAuthorize("hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<List<StudentProgressDto>> getStudentsProgressOfCourse(
      @PathVariable Long id) {
    List<StudentProgressDto> result = instructorCourseStudentService.getStudentsProgress(id);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/reviews")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getReviewsOfCourse(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return new ResponseEntity<>(iCourseService.getReviewOfCourse(id, page, limit), HttpStatus.OK);
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseEntity<CourseResponseDto> updateCourseStatus(
      @PathVariable Long id, @RequestBody CourseStatusUpdateRequest request) {
    CourseResponseDto dto =
        courseService.updateCourseStatus(id, request.getStatus(), request.getRejectedReason());
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/{id}/resubmit")
  @PreAuthorize("hasRole('ROLE_Instructor')")
  public ResponseEntity<CourseResponseDto> resubmitCourse(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
    CourseResponseDto dto = courseService.resubmitCourse(id, user.getUserId());
    return ResponseEntity.ok(dto);
  }
}
