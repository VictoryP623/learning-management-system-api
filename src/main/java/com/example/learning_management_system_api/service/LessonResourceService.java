package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.mapper.LessonResourceMapper;
import com.example.learning_management_system_api.dto.response.LessonResourceDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.entity.LessonResource;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.LessonResourceRepository;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.internal.FirebaseService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonResourceService {

  private final LessonRepository lessonRepository;
  private final LessonResourceRepository lessonResourceRepository;
  private final LessonResourceMapper lessonResourceMapper;

  // Cho phép tài liệu + video đính kèm
  private static final Set<String> allowedTypes =
      new HashSet<>(
          Arrays.asList(
              "image/jpeg",
              "image/png",
              "image/gif",
              "image/bmp",
              "image/webp",
              "application/pdf",
              "application/msword",
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
              "application/vnd.ms-excel",
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",

              // ✅ POWERPOINT
              "application/vnd.ms-powerpoint",
              "application/vnd.openxmlformats-officedocument.presentationml.presentation",

              // VIDEO
              "video/mp4",
              "video/webm",
              "video/quicktime",
              "application/octet-stream" // một số browser gửi mp4 kiểu này
              ));

  @Value("${firebase.bucket:}")
  private String firebaseBucket;

  private Credentials loadFirebaseCredentials() throws IOException {
    InputStream credentialsStream =
        FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
    if (credentialsStream == null) {
      throw new AppException(
          500, "firebase-key.json NOT FOUND. Put it in src/main/resources/firebase-key.json");
    }
    return GoogleCredentials.fromStream(credentialsStream);
  }

  private Storage buildStorageClient() throws IOException {
    if (firebaseBucket == null || firebaseBucket.isBlank()) {
      throw new AppException(500, "Missing config firebase.bucket in application.properties");
    }
    Credentials credentials = loadFirebaseCredentials();
    return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
  }

  /** Public để service khác reuse */
  public String uploadFileToFirebase(MultipartFile file, String firebasePath) throws IOException {
    Storage storage = buildStorageClient();

    String contentType = file.getContentType();
    if (contentType == null || contentType.isBlank()) {
      contentType = "application/octet-stream";
    }

    BlobId blobId = BlobId.of(firebaseBucket, firebasePath);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

    try {
      storage.create(blobInfo, file.getInputStream());
    } catch (StorageException se) {
      log.error(
          "Firebase Storage upload failed. bucket={}, path={}, code={}, msg={}",
          firebaseBucket,
          firebasePath,
          se.getCode(),
          se.getMessage(),
          se);
      throw new AppException(500, "Firebase upload failed: " + safeMsg(se.getMessage()));
    } catch (Exception e) {
      log.error(
          "Upload failed. bucket={}, path={}, err={}",
          firebaseBucket,
          firebasePath,
          e.getMessage(),
          e);
      throw new AppException(500, "Upload failed: " + safeMsg(e.getMessage()));
    }

    String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
    return String.format(
        DOWNLOAD_URL, firebaseBucket, URLEncoder.encode(firebasePath, StandardCharsets.UTF_8));
  }

  /** Public để service khác reuse */
  public boolean deleteFileFromFirebase(String url) {
    if (url == null || url.isBlank()) return true;
    if (!url.contains("firebasestorage.googleapis.com")) return true;

    try {
      Storage storage = buildStorageClient();

      String[] parts = url.split("/o/");
      if (parts.length < 2) return true;

      String fileName = parts[1].split("\\?")[0].replace("%2F", "/");
      BlobId blobId = BlobId.of(firebaseBucket, fileName);

      return storage.delete(blobId);
    } catch (Exception e) {
      log.error("Delete firebase file failed. url={}, err={}", url, e.getMessage(), e);
      return false;
    }
  }

  private String getExtension(String fileName) {
    if (fileName == null) return "";
    int idx = fileName.lastIndexOf(".");
    if (idx < 0) return "";
    return fileName.substring(idx);
  }

  private String detectTypeByFile(MultipartFile file) {
    String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
    String ct = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase();

    if (ct.startsWith("video/")
        || name.endsWith(".mp4")
        || name.endsWith(".webm")
        || name.endsWith(".mov")) {
      return "VIDEO";
    }
    if (name.endsWith(".pdf")) return "PDF";
    if (name.endsWith(".doc") || name.endsWith(".docx")) return "DOCX";
    if (name.endsWith(".xls") || name.endsWith(".xlsx")) return "XLSX";
    if (name.endsWith(".ppt") || name.endsWith(".pptx")) return "PPTX"; 
    if (name.endsWith(".jpg")
        || name.endsWith(".jpeg")
        || name.endsWith(".png")
        || name.endsWith(".gif")
        || name.endsWith(".webp")) {
      return "IMAGE";
    }
    return "OTHER";
  }

  private String safeMsg(String s) {
    if (s == null) return "unknown";
    return s.length() > 300 ? s.substring(0, 300) : s;
  }

  /**
   * Model A: - Nếu trùng resourceName trong cùng lesson => REPLACE (xóa file cũ + update url) -
   * Không trùng => create mới
   */
  public LessonResourceDto addLessonResource(
      MultipartFile multipartFile, Long lessonId, String resourceName) {

    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    // Validate content-type (fallback octet-stream)
    String contentType = multipartFile.getContentType();
    if (contentType == null || contentType.isBlank()) contentType = "application/octet-stream";
    if (!allowedTypes.contains(contentType)) {
      throw new AppException(
          415,
          "File type is not accepted. Only allow"
              + " image/pdf/doc/docx/xls/xlsx/ppt/pptx/mp4/webm/mov");
    }

    String displayName =
        (resourceName == null || resourceName.isBlank())
            ? Optional.ofNullable(multipartFile.getOriginalFilename()).orElse("Resource")
            : resourceName.trim();

    String origin = multipartFile.getOriginalFilename();
    String ext = getExtension(origin);
    String firebasePath = "resources/" + UUID.randomUUID() + ext;

    String newUrl;
    try {
      newUrl = uploadFileToFirebase(multipartFile, firebasePath);
    } catch (AppException ae) {
      throw ae;
    } catch (Exception e) {
      log.error("Unexpected upload error: {}", e.getMessage(), e);
      throw new AppException(500, "Upload resource failed.");
    }

    LessonResource existing =
        lessonResourceRepository
            .findFirstByLesson_IdAndNameIgnoreCase(lessonId, displayName)
            .orElse(null);

    if (existing != null) {
      if (existing.getUrl() != null && !existing.getUrl().isBlank()) {
        boolean ok = deleteFileFromFirebase(existing.getUrl());
        if (!ok) {
          log.warn("Replace resource: delete old file failed. oldUrl={}", existing.getUrl());
        }
      }
      existing.setUrl(newUrl);
      existing.setType(detectTypeByFile(multipartFile));
      LessonResource saved = lessonResourceRepository.save(existing);
      return lessonResourceMapper.toDto(saved);
    }

    LessonResource lr = new LessonResource();
    lr.setLesson(lesson);
    lr.setName(displayName);
    lr.setUrl(newUrl);
    lr.setType(detectTypeByFile(multipartFile));

    Integer maxOrder = lessonResourceRepository.findMaxOrderIndexByLessonId(lessonId);
    lr.setOrderIndex((maxOrder == null ? 0 : maxOrder) + 1);

    LessonResource saved = lessonResourceRepository.save(lr);
    return lessonResourceMapper.toDto(saved);
  }

  public void deleteResourceFile(Long lessonResourceId) {
    LessonResource lessonResource =
        lessonResourceRepository
            .findById(lessonResourceId)
            .orElseThrow(() -> new NoSuchElementException("Lesson resource not found"));

    boolean success = deleteFileFromFirebase(lessonResource.getUrl());
    if (!success) {
      throw new AppException(
          500, "Error while deleting file from Firebase. Please try again later.");
    }
    lessonResourceRepository.delete(lessonResource);
  }

  public PageDto getAllLessonResource(Long lessonId, int page, int size) {
    lessonRepository
        .findById(lessonId)
        .orElseThrow(() -> new NoSuchElementException("Not found lesson"));

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "orderIndex"));
    Page<LessonResource> p = lessonResourceRepository.findByLesson_Id(lessonId, pageable);

    List<LessonResourceDto> list =
        p.getContent().stream().map(lessonResourceMapper::toDto).toList();

    return new PageDto(
        p.getNumber(), p.getSize(), p.getTotalPages(), p.getTotalElements(), new ArrayList<>(list));
  }

  public LessonResourceDto getLessonResource(Long id) {
    LessonResource lessonResource =
        lessonResourceRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson resource not found"));
    return lessonResourceMapper.toDto(lessonResource);
  }
}
