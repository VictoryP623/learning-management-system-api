package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.response.StudentAssignmentTimelineDto;
import com.example.learning_management_system_api.service.StudentAssignmentTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
public class StudentAssignmentTimelineController {

  private final StudentAssignmentTimelineService studentAssignmentTimelineService;

  @PreAuthorize("hasRole('ROLE_Student')")
  @GetMapping("/{courseId}/assignments")
  public ResponseEntity<StudentAssignmentTimelineDto> getTimeline(
      @PathVariable Long courseId, @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long userId = userDetails.getUser().getId(); // tùy CustomUserDetails của bạn
    return ResponseEntity.ok(
        studentAssignmentTimelineService.getTimelineByCourse(courseId, userId));
  }
}
