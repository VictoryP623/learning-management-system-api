package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.LessonMapper;
import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.entity.Purchase;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.PurchaseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LessonService implements ILessonService {

  private final LessonRepository lessonRepository;
  private final CourseRepository courseRepository;
  private final LessonMapper lessonMapper;
  private final PurchaseRepository purchaseRepository;

  public LessonService(
      LessonRepository lessonRepository,
      CourseRepository courseRepository,
      LessonMapper lessonMapper,
      PurchaseRepository purchaseRepository) {
    this.lessonRepository = lessonRepository;
    this.courseRepository = courseRepository;
    this.lessonMapper = lessonMapper;
    this.purchaseRepository = purchaseRepository;
  }

  public LessonResponseDto createLesson(LessonRequestDto requestDTO) {
    Course course =
        courseRepository
            .findById(requestDTO.courseId())
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "Course not found with ID: " + requestDTO.courseId()));
    if (lessonRepository.existsByNameAndCourseId(requestDTO.name(), requestDTO.courseId())) {
      throw new DuplicateKeyException(
          "There is already a lesson with the name '"
              + requestDTO.name()
              + "' in course ID: "
              + requestDTO.courseId());
    }
    checkPermission(course);
    Lesson lesson = lessonMapper.toEntity(requestDTO);
    lesson.setCourse(course);

    Lesson savedLesson = lessonRepository.save(lesson);

    return lessonMapper.toDto(savedLesson);
  }

  public LessonResponseDto getLessonById(Long id) {
    Lesson lesson =
        lessonRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found with ID: " + id));
    checkGetPermission(lesson.getCourse());
    return lessonMapper.toDto(lesson);
  }

  public List<LessonResponseDto> getAllLessons(Long courseId, String name) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NoSuchElementException("Course not found with ID: " + courseId));
    checkGetPermission(course);
    List<Lesson> lessonList =
        lessonRepository.findByCourse_IdAndNameContaining(courseId, name == null ? "" : name);
    return lessonList.stream().map(lessonMapper::toDto).collect(Collectors.toList());
  }

  public LessonResponseDto updateLesson(Long id, LessonRequestDto requestDTO) {
    Lesson lesson =
        lessonRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found with ID: " + id));
    lessonMapper.updateLessonEntity(requestDTO, lesson);
    checkPermission(lesson.getCourse());
    if (requestDTO.courseId() != null) {
      Course course =
          courseRepository
              .findById(requestDTO.courseId())
              .orElseThrow(
                  () ->
                      new NoSuchElementException(
                          "Course not found with ID: " + requestDTO.courseId()));
      checkPermission(course);
      lesson.setCourse(course);
    }
    if (requestDTO.name() != null) {
      if (lessonRepository.existsByNameAndCourseIdAndIdNot(
          requestDTO.name(), lesson.getCourse().getId(), lesson.getId())) {
        throw new DuplicateKeyException(
            "There is already a lesson with the name '"
                + requestDTO.name()
                + "' in course ID: "
                + lesson.getCourse().getId());
      }
    }

    Lesson updatedLesson = lessonRepository.save(lesson);
    return lessonMapper.toDto(updatedLesson);
  }

  public void deleteLesson(Long id) {
    Lesson lesson =
        lessonRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found with ID: " + id));
    checkPermission(lesson.getCourse());
    lessonRepository.delete(lesson);
  }

  @SneakyThrows
  void checkPermission(Course course) {
    boolean isAllowed = false;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      if (Objects.equals(customUserDetails.getUserId(), course.getInstructor().getUser().getId())) {
        isAllowed = true;
      }
    }
    if (!isAllowed) {
      throw new AppException(403, "Access denied. You are not instructor of this course");
    }
  }

  @SneakyThrows
  public void checkGetPermission(Course course) {
    boolean isAllowed = false;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();

      // Admin
      if (customUserDetails.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin"))) {
        isAllowed = true;
      }
      // Owner
      else if (userId.equals(course.getInstructor().getUser().getId())) {
        isAllowed = true;
      }
      // Check if this user have purchased
      else {
        List<Purchase> purchaseList =
            purchaseRepository.findDistinctCoursesByStudent_User_IdAndIsPaidTrue(userId);
        List<Course> boughtCourse =
            purchaseList.stream()
                .flatMap(purchase -> purchase.getCourses().stream())
                .distinct()
                .toList();
        if (boughtCourse.contains(course)) {
          isAllowed = true;
        }
      }
    }
    if (!isAllowed) {
      throw new AppException(
          403,
          "Access denied. You cannot access this course because you are neither the owner of this"
              + " course nor have you purchased it.");
    }
  }
}
