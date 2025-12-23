package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.AssignmentRequestDto;
import com.example.learning_management_system_api.dto.request.AssignmentSubmissionRequestDto;
import com.example.learning_management_system_api.dto.request.GradeSubmissionRequestDto;
import com.example.learning_management_system_api.dto.response.AssignmentResponseDto;
import com.example.learning_management_system_api.dto.response.AssignmentSubmissionResponseDto;
import com.example.learning_management_system_api.service.AssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

  private final AssignmentService assignmentService;

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @PostMapping
  public ResponseEntity<AssignmentResponseDto> createAssignment(
      @Valid @RequestBody AssignmentRequestDto dto) {
    return ResponseEntity.ok(assignmentService.createAssignment(dto));
  }

  // Lấy chi tiết 1 assignment
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<AssignmentResponseDto> getById(@PathVariable Long id) {
    return ResponseEntity.ok(assignmentService.getAssignmentById(id));
  }

  // Lấy danh sách assignment theo lesson
  @GetMapping("/by-lesson/{lessonId}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<List<AssignmentResponseDto>> getByLesson(@PathVariable Long lessonId) {
    return ResponseEntity.ok(assignmentService.getAssignmentsByLesson(lessonId));
  }

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @PatchMapping("/{id}")
  public ResponseEntity<AssignmentResponseDto> update(
      @PathVariable Long id, @Valid @RequestBody AssignmentRequestDto dto) {
    return ResponseEntity.ok(assignmentService.updateAssignment(id, dto));
  }

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    assignmentService.deleteAssignment(id);
    return ResponseEntity.noContent().build();
  }

  // Student submit
  @PreAuthorize("hasRole('ROLE_Student')")
  @PostMapping("/submit")
  public ResponseEntity<AssignmentSubmissionResponseDto> submit(
      @Valid @RequestBody AssignmentSubmissionRequestDto dto) {
    return ResponseEntity.ok(assignmentService.submitAssignment(dto));
  }

  // Student xem bài nộp của chính mình
  @PreAuthorize("hasRole('ROLE_Student')")
  @GetMapping("/{assignmentId}/my-submission")
  public ResponseEntity<AssignmentSubmissionResponseDto> getMySubmission(
      @PathVariable Long assignmentId) {
    AssignmentSubmissionResponseDto dto =
        assignmentService.getMySubmissionForAssignment(assignmentId);
    if (dto == null) {
      return ResponseEntity.noContent().build(); // 204, chưa nộp
    }
    return ResponseEntity.ok(dto);
  }

  // Instructor xem danh sách bài nộp
  @PreAuthorize("hasRole('ROLE_Instructor')")
  @GetMapping("/{assignmentId}/submissions")
  public ResponseEntity<List<AssignmentSubmissionResponseDto>> getSubmissions(
      @PathVariable Long assignmentId) {
    return ResponseEntity.ok(assignmentService.getSubmissionsOfAssignment(assignmentId));
  }

  // Instructor chấm điểm
  @PreAuthorize("hasRole('ROLE_Instructor')")
  @PostMapping("/grade")
  public ResponseEntity<AssignmentSubmissionResponseDto> grade(
      @Valid @RequestBody GradeSubmissionRequestDto dto) {
    return ResponseEntity.ok(assignmentService.gradeSubmission(dto));
  }
}
