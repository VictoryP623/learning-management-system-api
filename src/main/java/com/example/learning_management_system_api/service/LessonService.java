package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.LessonMapper;
import com.example.learning_management_system_api.dto.request.LessonRequestDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Lesson;
import com.example.learning_management_system_api.entity.LessonCompletion;
import com.example.learning_management_system_api.entity.LessonResource;
import com.example.learning_management_system_api.entity.Purchase;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.events.StudentEvents;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.LessonCompletionRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.LessonResourceRepository;
import com.example.learning_management_system_api.repository.PurchaseRepository;
import com.example.learning_management_system_api.repository.QuizAttemptRepository;
import com.example.learning_management_system_api.repository.QuizRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.example.learning_management_system_api.utils.enums.LessonProgressStatus;
import com.example.learning_management_system_api.utils.enums.LessonUnlockType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService implements ILessonService {

  private final LessonRepository lessonRepository;
  private final CourseRepository courseRepository;
  private final LessonMapper lessonMapper;
  private final PurchaseRepository purchaseRepository;
  private final QuizRepository quizRepository;
  private final QuizAttemptRepository quizAttemptRepository;
  private final LessonResourceRepository lessonResourceRepository;
  private final LessonCompletionRepository lessonCompletionRepository;
  private final StudentRepository studentRepository;
  private final LessonResourceService lessonResourceService;
  private final ApplicationEventPublisher publisher;

  public LessonService(
      LessonRepository lessonRepository,
      CourseRepository courseRepository,
      LessonMapper lessonMapper,
      PurchaseRepository purchaseRepository,
      LessonResourceRepository lessonResourceRepository,
      QuizRepository quizRepository,
      QuizAttemptRepository quizAttemptRepository,
      LessonCompletionRepository lessonCompletionRepository,
      StudentRepository studentRepository,
      LessonResourceService lessonResourceService,
      ApplicationEventPublisher publisher) {
    this.lessonResourceService = lessonResourceService;
    this.studentRepository = studentRepository;
    this.lessonCompletionRepository = lessonCompletionRepository;
    this.lessonRepository = lessonRepository;
    this.courseRepository = courseRepository;
    this.lessonMapper = lessonMapper;
    this.purchaseRepository = purchaseRepository;
    this.lessonResourceRepository = lessonResourceRepository;
    this.quizRepository = quizRepository;
    this.quizAttemptRepository = quizAttemptRepository;
    this.publisher = publisher;
  }

  // ✅ IMPORTANT: cần @Transactional vì notifier dùng AFTER_COMMIT
  @Transactional
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
    lesson.setIsFree(requestDTO.isFree());
    lesson.setVideoUrl(requestDTO.resourceUrl()); // dùng resourceUrl từ request làm videoUrl chính

    long count = lessonRepository.countByCourseId(course.getId());
    lesson.setOrderIndex((int) count + 1);

    if (lesson.getUnlockType() == null) {
      lesson.setUnlockType(LessonUnlockType.NONE);
    }

    Lesson savedLesson = lessonRepository.save(lesson);

    Long courseId = savedLesson.getCourse().getId();

    // ✅ FIX: publish instructorUserId (User.id), không phải Instructor.id
    Long instructorUserId =
        savedLesson.getCourse().getInstructor() != null
                && savedLesson.getCourse().getInstructor().getUser() != null
            ? savedLesson.getCourse().getInstructor().getUser().getId()
            : null;

    publisher.publishEvent(
        new StudentEvents.LessonCreatedEvent(courseId, savedLesson.getId(), instructorUserId));

    return lessonMapper.toDto(savedLesson);
  }

  public LessonResponseDto getLessonById(Long id) {
    Lesson lesson =
        lessonRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found with ID: " + id));

    checkGetPermission(lesson.getCourse());

    LessonResponseDto dto = lessonMapper.toDto(lesson);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {

      Long userId = customUserDetails.getUserId();
      Student student = studentRepository.findByUserId(userId).orElse(null);

      if (student != null) {
        Long studentId = student.getId();
        boolean completed =
            lessonCompletionRepository.existsByStudentIdAndLessonIdAndStatus(
                studentId, id, LessonProgressStatus.COMPLETED);
        dto.setCompleted(completed);

        boolean canAccess = canAccessLesson(studentId, lesson);
        dto.setLocked(!canAccess);
      }
    }

    return dto;
  }

  public List<LessonResponseDto> getAllLessons(Long courseId, String name) {
    List<Lesson> lessonList =
        lessonRepository.findByCourse_IdAndNameContaining(courseId, name == null ? "" : name);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Long currentStudentId = null;
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();
      Student student = studentRepository.findByUserId(userId).orElse(null);
      if (student != null) {
        currentStudentId = student.getId();
      }
    }

    Long finalCurrentStudentId = currentStudentId;

    return lessonList.stream()
        .map(
            lesson -> {
              LessonResponseDto dto = lessonMapper.toDto(lesson);

              if (Boolean.TRUE.equals(lesson.getIsFree())) {
                List<LessonResource> resources =
                    lessonResourceRepository.findByLesson_IdOrderByOrderIndexAsc(lesson.getId());

                if (!resources.isEmpty()) {
                  LessonResource videoResource =
                      resources.stream()
                          .filter(
                              r -> r.getUrl() != null && r.getUrl().toLowerCase().endsWith(".mp4"))
                          .findFirst()
                          .orElse(resources.get(0));
                  dto.setResourceUrl(videoResource.getUrl());
                }
              } else {
                dto.setResourceUrl(null);
              }

              if (finalCurrentStudentId != null) {
                boolean completed =
                    lessonCompletionRepository.existsByStudentIdAndLessonIdAndStatus(
                        finalCurrentStudentId, lesson.getId(), LessonProgressStatus.COMPLETED);
                dto.setCompleted(completed);

                boolean canAccess = canAccessLesson(finalCurrentStudentId, lesson);
                dto.setLocked(!canAccess);
              }

              return dto;
            })
        .collect(Collectors.toList());
  }

  // ✅ IMPORTANT: cần @Transactional vì notifier dùng AFTER_COMMIT
  @Transactional
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

    lesson.setIsFree(requestDTO.isFree());
    if (requestDTO.resourceUrl() != null) {
      lesson.setVideoUrl(requestDTO.resourceUrl());
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

    publisher.publishEvent(
        new StudentEvents.LessonUpdatedEvent(
            updatedLesson.getCourse().getId(), updatedLesson.getId()));

    return lessonMapper.toDto(updatedLesson);
  }

  @Transactional
  public void deleteLesson(Long id) {
    Lesson lesson =
        lessonRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found with ID: " + id));
    checkPermission(lesson.getCourse());

    quizAttemptRepository.deleteByLessonId(id);
    quizRepository.deleteByLessonId(id);

    List<LessonResource> resources =
        lessonResourceRepository.findByLesson_IdOrderByOrderIndexAsc(id);
    for (LessonResource r : resources) {
      if (r.getUrl() != null) {
        lessonResourceService.deleteFileFromFirebase(r.getUrl());
      }
      lessonResourceRepository.delete(r);
    }
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

      if (customUserDetails.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals("ROLE_Admin"))) {
        isAllowed = true;
      } else if (userId.equals(course.getInstructor().getUser().getId())) {
        isAllowed = true;
      } else {
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

  public boolean canAccessLesson(Long studentId, Lesson lesson) {

    LessonUnlockType type = lesson.getUnlockType();

    if (type == null || type == LessonUnlockType.NONE) {
      return true;
    }

    if (type == LessonUnlockType.PREVIOUS_COMPLETED) {
      Lesson previous =
          lessonRepository.findFirstByCourse_IdAndOrderIndexLessThanOrderByOrderIndexDesc(
              lesson.getCourse().getId(), lesson.getOrderIndex());

      if (previous == null) {
        return true;
      }

      return lessonCompletionRepository.existsByStudentIdAndLessonIdAndStatus(
          studentId, previous.getId(), LessonProgressStatus.COMPLETED);
    }

    if (type == LessonUnlockType.SPECIFIC_LESSON_COMPLETED) {
      if (lesson.getRequiredLessonId() == null) return true;

      return lessonCompletionRepository.existsByStudentIdAndLessonIdAndStatus(
          studentId, lesson.getRequiredLessonId(), LessonProgressStatus.COMPLETED);
    }

    return true;
  }

  public LessonResponseDto completeLesson(Long studentId, Long lessonId) {

    Lesson lesson =
        lessonRepository
            .findById(lessonId)
            .orElseThrow(() -> new NoSuchElementException("Lesson not found"));

    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new NoSuchElementException("Student not found"));

    LessonCompletion completion =
        lessonCompletionRepository.findByStudentIdAndLessonId(studentId, lessonId);

    if (completion == null) {
      completion = new LessonCompletion();
      completion.setLesson(lesson);
      completion.setStudent(student);
    }

    completion.setStatus(LessonProgressStatus.COMPLETED);
    completion.setCompletedAt(LocalDateTime.now());
    completion.setWatchedSeconds(lesson.getDurationSec());
    lessonCompletionRepository.save(completion);

    LessonResponseDto response = lessonMapper.toDto(lesson);
    response.setCompleted(true);

    Lesson next =
        lessonRepository.findFirstByCourse_IdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
            lesson.getCourse().getId(), lesson.getOrderIndex());

    if (next != null) {
      boolean canAccessNext = canAccessLesson(studentId, next);
      response.setNextLessonId(canAccessNext ? next.getId() : null);
      response.setNextLessonLocked(!canAccessNext);
      response.setNextLessonName(next.getName());
    }

    return response;
  }
}
