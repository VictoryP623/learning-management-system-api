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
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.internal.FirebaseService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LessonResourceService {

  private final LessonRepository lessonRepository;
  private final LessonResourceRepository lessonResourceRepository;
  private final LessonResourceMapper lessonResourceMapper;

  private static final List<String> allowedTypes =
      Arrays.asList(
          "image/jpeg", // JPEG format
          "image/png", // PNG format
          "image/gif", // GIF format
          "image/bmp", // BMP format
          "image/webp", // WEBP format
          "application/pdf", // PDF format
          "application/msword", // Microsoft Word (.doc)
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
          "application/vnd.ms-excel", // .xls
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
          );

  @Value("${firebase.bucket}")
  String firebaseBucket;

  public LessonResourceService(
      LessonRepository lessonRepository,
      LessonResourceRepository lessonResourceRepository,
      LessonResourceMapper lessonResourceMapper) {
    this.lessonRepository = lessonRepository;
    this.lessonResourceRepository = lessonResourceRepository;
    this.lessonResourceMapper = lessonResourceMapper;
  }

  String uploadFileToFirebase(MultipartFile file, String fileName) throws IOException {
    BlobId blobId = BlobId.of(firebaseBucket, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

    InputStream credentialsStream =
        FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
    Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    storage.create(blobInfo, file.getInputStream());

    String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media";
    return String.format(
        DOWNLOAD_URL, firebaseBucket, URLEncoder.encode(fileName, StandardCharsets.UTF_8));
  }

  @SneakyThrows
  public LessonResourceDto addLessonResource(
      MultipartFile multipartFile, Long lessonId, String resourceName) {
    String fileName = multipartFile.getOriginalFilename();
    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    // Xoá resource cũ (nếu lesson đã có) – nếu bạn muốn cho nhiều tài liệu thì bỏ đoạn này
    List<LessonResource> oldResources = lessonResourceRepository.findByLessonId(lessonId);
    for (LessonResource r : oldResources) {
      if (r.getUrl() != null && !r.getUrl().isEmpty()) {
        deleteFileFromFirebase(r.getUrl());
      }
      lessonResourceRepository.delete(r);
    }

    // Kiểm tra loại file
    String fileType = multipartFile.getContentType();
    if (!allowedTypes.contains(fileType)) {
      throw new AppException(
          415,
          "File type is not accept. Only allow (.jpeg, .png, .gif, .bmp, .webp, .pdf, .doc, .docx,"
              + " .xls, .xlsx)");
    }

    // Sinh tên file lưu trên Firebase (tránh trùng)
    fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));
    fileName = "resources/" + fileName;

    // Upload lên Firebase Storage
    String url = uploadFileToFirebase(multipartFile, fileName);

    // Lưu vào DB
    LessonResource lessonResource = new LessonResource();
    lessonResource.setLesson(lesson);
    lessonResource.setName(resourceName);
    lessonResource.setUrl(url);
    // nếu bạn đã thêm type/orderIndex trong LessonResource thì có thể set ở đây

    return lessonResourceMapper.toDto(lessonResourceRepository.save(lessonResource));
  }

  public void deleteResourceFile(Long lessonResourceId) {
    LessonResource lessonResource =
        lessonResourceRepository
            .findById(lessonResourceId)
            .orElseThrow(() -> new NoSuchElementException("Lesson resource not found"));

    // Xoá file trên firebase
    boolean success = deleteFileFromFirebase(lessonResource.getUrl());
    if (!success) {
      throw new RuntimeException("Error while delete file from Firebase. Please try again later");
    }

    // Xóa resource trong DB
    lessonResourceRepository.delete(lessonResource);

    // Không cần đụng tới Lesson.videoUrl nữa
    // (attachments tách riêng, video chính set qua LessonRequestDto)
  }

  public PageDto getAllLessonResource(Long lessonId, int page, int size) {
    lessonRepository
        .findById(lessonId)
        .orElseThrow(() -> new NoSuchElementException("Not found lesson"));
    Pageable pageable = PageRequest.of(page, size);
    Page<LessonResource> lessonResourcePage =
        lessonResourceRepository.findByLessonId(lessonId, pageable);
    List<LessonResourceDto> lessonResourceDtoList =
        lessonResourcePage.getContent().stream().map(lessonResourceMapper::toDto).toList();
    return new PageDto(
        lessonResourcePage.getNumber(),
        lessonResourcePage.getSize(),
        lessonResourcePage.getTotalPages(),
        lessonResourcePage.getTotalElements(),
        new ArrayList<>(lessonResourceDtoList));
  }

  public LessonResourceDto getLessonResource(Long id) {
    LessonResource lessonResource =
        lessonResourceRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson resource not found"));
    return lessonResourceMapper.toDto(lessonResource);
  }

  @SneakyThrows
  boolean deleteFileFromFirebase(String url) {
    String[] parts = url.split("/o/");
    String fileName = parts[1].split("\\?")[0];
    fileName = fileName.replace("%2F", "/");
    BlobId blobId = BlobId.of(firebaseBucket, fileName);
    InputStream credentialsStream =
        FirebaseService.class.getClassLoader().getResourceAsStream("firebase-key.json");
    Credentials credentials = GoogleCredentials.fromStream(credentialsStream);
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    return storage.delete(blobId);
  }

  private String getExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf("."));
  }
}
