package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.service.LessonVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonVideoController {

  private final LessonVideoService lessonVideoService;

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @PostMapping("/{lessonId}/video")
  public ResponseEntity<LessonResponseDto> uploadVideo(
      @PathVariable Long lessonId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "durationSec", required = false) Integer durationSec) {

    return ResponseEntity.ok(lessonVideoService.uploadOrReplaceVideo(lessonId, file, durationSec));
  }

  @PreAuthorize("hasRole('ROLE_Instructor')")
  @DeleteMapping("/{lessonId}/video")
  public ResponseEntity<LessonResponseDto> deleteVideo(@PathVariable Long lessonId) {
    return ResponseEntity.ok(lessonVideoService.deleteVideo(lessonId));
  }
}
