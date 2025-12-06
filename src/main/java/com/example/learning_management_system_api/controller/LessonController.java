package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.example.learning_management_system_api.service.ILessonService;
import com.example.learning_management_system_api.service.LessonService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

  private final ILessonService lessonService;
  private final StudentRepository studentRepository;
  private final LessonService lessonServiceImpl;

  public LessonController(
      ILessonService lessonService,
      StudentRepository studentRepository,
      LessonService lessonServiceImpl) {
    this.lessonService = lessonServiceImpl; // dùng implement
    this.studentRepository = studentRepository;
    this.lessonServiceImpl = lessonServiceImpl;
  }

  @PreAuthorize(" hasRole('ROLE_Instructor') ")
  @PostMapping
  public ResponseEntity<LessonResponseDto> createLesson(
      @RequestBody @Valid LessonRequestDto requestDTO) {
    LessonResponseDto lessonResponse = lessonService.createLesson(requestDTO);
    return ResponseEntity.ok(lessonResponse);
  }

  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  @GetMapping("/{id}")
  public ResponseEntity<LessonResponseDto> getLessonById(@PathVariable Long id) {
    LessonResponseDto lessonResponse = lessonService.getLessonById(id);
    return ResponseEntity.ok(lessonResponse);
  }

  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  @GetMapping
  public ResponseEntity<List<LessonResponseDto>> getAllLessons(
      @RequestParam Long courseId, @RequestParam(required = false) String name) {
    List<LessonResponseDto> lessons = lessonService.getAllLessons(courseId, name);
    return ResponseEntity.ok(lessons);
  }

  @PreAuthorize(" hasRole('ROLE_Instructor')")
  @PatchMapping("/{id}")
  public ResponseEntity<LessonResponseDto> updateLesson(
      @PathVariable Long id, @Validated @RequestBody LessonRequestDto requestDTO) {
    LessonResponseDto lessonResponse = lessonService.updateLesson(id, requestDTO);
    return ResponseEntity.ok(lessonResponse);
  }

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
    lessonService.deleteLesson(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{lessonId}/complete")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<?> markLessonCompleted(@PathVariable Long lessonId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }
    Long userId = customUserDetails.getUserId();

    Long studentId =
        studentRepository
            .findByUserId(userId)
            .map(Student::getId)
            .orElseThrow(() -> new RuntimeException("Student not found for userId: " + userId));

    LessonResponseDto response = lessonServiceImpl.completeLesson(studentId, lessonId);
    return ResponseEntity.ok(response);
  }
}
