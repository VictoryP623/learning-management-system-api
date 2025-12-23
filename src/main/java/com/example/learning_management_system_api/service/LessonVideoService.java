package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.LessonMapper;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.LessonRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonVideoService {

  private final LessonRepository lessonRepository;
  private final LessonResourceService lessonResourceService;
  private final LessonMapper lessonMapper;

  public LessonResponseDto uploadOrReplaceVideo(
      Long lessonId, MultipartFile file, Integer durationSec) {
    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    String ct = (file.getContentType() == null) ? "" : file.getContentType().toLowerCase();
    String name =
        (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename().toLowerCase();

    boolean isVideo =
        ct.startsWith("video/")
            || name.endsWith(".mp4")
            || name.endsWith(".webm")
            || name.endsWith(".mov");

    if (!isVideo) {
      throw new AppException(415, "Video file is required (mp4/webm/mov).");
    }

    // delete old video
    if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isBlank()) {
      boolean ok = lessonResourceService.deleteFileFromFirebase(lesson.getVideoUrl());
      if (!ok) log.warn("Delete old video failed: {}", lesson.getVideoUrl());
    }

    // upload new video
    String ext = "";
    int idx = name.lastIndexOf(".");
    if (idx >= 0) ext = name.substring(idx);

    String firebasePath = "videos/" + UUID.randomUUID() + ext;

    String url;
    try {
      url = lessonResourceService.uploadFileToFirebase(file, firebasePath);
    } catch (AppException ae) {
      throw ae;
    } catch (Exception e) {
      log.error("Upload video failed: {}", e.getMessage(), e);
      throw new AppException(500, "Upload video failed.");
    }

    lesson.setVideoUrl(url);
    if (durationSec != null && durationSec >= 0) {
      lesson.setDurationSec(durationSec);
    }

    Lesson saved = lessonRepository.save(lesson);
    return lessonMapper.toDto(saved);
  }

  public LessonResponseDto deleteVideo(Long lessonId) {
    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isBlank()) {
      boolean ok = lessonResourceService.deleteFileFromFirebase(lesson.getVideoUrl());
      if (!ok) {
        throw new AppException(500, "Delete video failed on Firebase.");
      }
    }
    lesson.setVideoUrl(null);
    Lesson saved = lessonRepository.save(lesson);
    return lessonMapper.toDto(saved);
  }
}
