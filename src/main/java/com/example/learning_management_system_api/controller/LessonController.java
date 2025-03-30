package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.service.ILessonService;
import com.example.learning_management_system_api.service.LessonService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final ILessonService lessonService;

    public LessonController(ILessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PreAuthorize(" hasRole('ROLE_Instructor') ")
    @PostMapping
    public ResponseEntity<LessonResponseDto> createLesson(@RequestBody @Valid LessonRequestDto requestDTO) {
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
    public ResponseEntity<List<LessonResponseDto>> getAllLessons(@RequestParam Long courseId,
                                                                 @RequestParam(required = false) String name) {
        List<LessonResponseDto> lessons = lessonService.getAllLessons(courseId, name);
        return ResponseEntity.ok(lessons);
    }

    @PreAuthorize(" hasRole('ROLE_Instructor')")
    @PatchMapping("/{id}")
    public ResponseEntity<LessonResponseDto> updateLesson(
            @PathVariable Long id,
            @Validated @RequestBody LessonRequestDto requestDTO) {
        LessonResponseDto lessonResponse = lessonService.updateLesson(id, requestDTO);
        return ResponseEntity.ok(lessonResponse);
    }

    // Delete a lesson by ID
    @PreAuthorize("hasRole('ROLE_Instructor')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
