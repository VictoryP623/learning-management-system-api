package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.dto.response.StudentProgressDto;
import com.example.learning_management_system_api.service.InstructorCourseStudentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Endpoint cho Instructor xem danh sách học viên & tiến độ của 1 khoá

@RestController
@RequestMapping("/api/instructor/courses")
@RequiredArgsConstructor
public class InstructorCourseStudentController {

  private final InstructorCourseStudentService instructorCourseStudentService;

  @GetMapping("/{courseId}/students-progress")
  @PreAuthorize("hasRole('ROLE_Instructor')")
  public ResponseEntity<ResponseVO<List<StudentProgressDto>>> getStudentsProgress(
      @PathVariable Long courseId) {

    List<StudentProgressDto> result = instructorCourseStudentService.getStudentsProgress(courseId);

    return ResponseEntity.ok(ResponseVO.success(result));
  }
}
