package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.RubricRequestDto;
import com.example.learning_management_system_api.dto.response.RubricResponseDto;
import com.example.learning_management_system_api.service.RubricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments/{assignmentId}/rubric")
@RequiredArgsConstructor
public class RubricController {

  private final RubricService rubricService;

  // Instructor tạo / cập nhật rubric cho assignment
  @PreAuthorize("hasRole('ROLE_Instructor')")
  @PostMapping
  public ResponseEntity<RubricResponseDto> createOrUpdate(
      @PathVariable Long assignmentId, @Valid @RequestBody RubricRequestDto dto) {
    return ResponseEntity.ok(rubricService.createOrUpdateRubric(assignmentId, dto));
  }

  // Instructor + Student xem rubric của assignment
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  @GetMapping
  public ResponseEntity<RubricResponseDto> getRubric(@PathVariable Long assignmentId) {
    return ResponseEntity.ok(rubricService.getRubricByAssignmentId(assignmentId));
  }
}
