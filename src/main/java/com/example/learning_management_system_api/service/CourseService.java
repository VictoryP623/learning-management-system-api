package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.mapper.LessonMapper;
import com.example.learning_management_system_api.dto.mapper.ReviewMapper;
import com.example.learning_management_system_api.dto.mapper.StudentMapper;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ReviewResponseDto;
import com.example.learning_management_system_api.dto.response.StudentResponseDto;
import com.example.learning_management_system_api.entity.*;
import com.example.learning_management_system_api.events.InstructorEvents;
import com.example.learning_management_system_api.events.StudentEvents;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.EnrollRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.LessonCompletionRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.example.learning_management_system_api.utils.enums.LessonProgressStatus;
import com.example.learning_management_system_api.utils.enums.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationEventPublisher; // NEW
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService implements ICourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final StudentMapper studentMapper;
  private final EnrollRepository enrollRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final InstructorRepository instructorRepository;
  private final LessonRepository lessonRepository;
  private final LessonCompletionRepository lessonCompletionRepository;
  private final StudentRepository studentRepository;
  private final LessonMapper lessonMapper;
  private final ApplicationEventPublisher publisher;

  public CourseService(
      CourseRepository courseRepository,
      CourseMapper courseMapper,
      StudentMapper studentMapper,
      EnrollRepository enrollRepository,
      ReviewRepository reviewRepository,
      ReviewMapper reviewMapper,
      InstructorRepository instructorRepository,
      LessonRepository lessonRepository,
      LessonCompletionRepository lessonCompletionRepository,
      StudentRepository studentRepository,
      LessonMapper lessonMapper,
      ApplicationEventPublisher publisher) {
    this.lessonMapper = lessonMapper;
    this.studentRepository = studentRepository;
    this.lessonRepository = lessonRepository;
    this.lessonCompletionRepository = lessonCompletionRepository;
    this.instructorRepository = instructorRepository;
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.studentMapper = studentMapper;
    this.enrollRepository = enrollRepository;
    this.reviewRepository = reviewRepository;
    this.reviewMapper = reviewMapper;
    this.publisher = publisher;
  }

  // Return list of course with metadata about paging
  public PageDto getAllCourse(
      int page,
      int limit,
      String courseName,
      String categoryName,
      Double price,
      Long instructorId,
      CustomUserDetails userDetails // Có thể null!
      ) {
    Specification<Course> spec = courseFilter(courseName, categoryName, price, instructorId);

    if (userDetails == null) {
      // Chưa đăng nhập: chỉ trả về course đã duyệt
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "APPROVED"));
    } else {
      User user = userDetails.getUser();
      UserRole role = user.getRole();
      Long userId = user.getId();

      if (role == UserRole.Student) {
        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "APPROVED"));
      } else if (role == UserRole.Instructor) {
        // Instructor: chỉ trả về khóa học của instructor này
        Instructor instructor = instructorRepository.findByUserId(userId);
        if (instructor == null)
          throw new RuntimeException("Instructor not found for userId: " + userId);
        spec =
            spec.and(
                (root, query, cb) ->
                    cb.equal(root.get("instructor").get("id"), instructor.getId()));
      }
      // Admin thì không cần filter thêm: trả về tất cả courses
    }

    Page<Course> coursePage = courseRepository.findAll(spec, PageRequest.of(page, limit));

    // Lấy rating trung bình cho từng khoá học
    List<CourseResponseDto> result =
        coursePage.getContent().stream()
            .map(
                course -> {
                  Double avgRating = reviewRepository.avgRatingByCourseId(course.getId());
                  if (avgRating == null) avgRating = 0.0;
                  return new CourseResponseDto(
                      course.getId(),
                      course.getInstructor().getUser().getId(),
                      course.getInstructor().getUser().getFullname(),
                      course.getCategory().getName(),
                      course.getPrice(),
                      course.getCreatedAt(),
                      course.getUpdatedAt(),
                      course.getThumbnail(),
                      course.getStatus(),
                      course.getName(),
                      course.getCategory().getId(),
                      course.getRejectedReason(),
                      null, // lessons
                      null, // completedLessons
                      null, // totalLessons
                      avgRating // rating\
                      );
                })
            .toList();

    return new PageDto(
        coursePage.getNumber(),
        coursePage.getSize(),
        coursePage.getTotalPages(),
        coursePage.getTotalElements(),
        new ArrayList<>(result));
  }

  @SneakyThrows
  public CourseResponseDto getCourse(Long id, CustomUserDetails userDetails) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(
                () -> new NoSuchElementException("Course with id " + id + " is not found"));

    // ===== Access control theo role + ownership =====
    // Guest không vào được vì @PreAuthorize đã chặn (chỉ có Student/Instructor/Admin)
    UserRole role = userDetails.getUser().getRole();
    Long viewerUserId = userDetails.getUserId();

    String status = course.getStatus(); // "APPROVED" | "PENDING" | "REJECTED"

    // lấy owner userId của instructor
    Long ownerUserId =
        course.getInstructor() != null && course.getInstructor().getUser() != null
            ? course.getInstructor().getUser().getId()
            : null;

    boolean isApproved = "APPROVED".equalsIgnoreCase(status);
    boolean isAdmin = role == UserRole.Admin;
    boolean isOwnerInstructor =
        (role == UserRole.Instructor) && ownerUserId != null && ownerUserId.equals(viewerUserId);

    if (!isApproved) {
      if (!(isAdmin || isOwnerInstructor)) {
        // Không tiết lộ existence -> NotFound là hợp lý
        throw new NoSuchElementException("Course with id " + id + " is not found");
        // hoặc bạn muốn rõ ràng: throw new AccessDeniedException("Not allowed");
      }
    }

    // ===== Từ đây trở xuống giữ nguyên logic hiện tại của bạn =====

    List<Lesson> lessonEntities = lessonRepository.findByCourse_Id(course.getId());

    // chỉ student mới cần completed flag
    Long studentId = null;
    if (role == UserRole.Student) {
      Optional<Student> student = studentRepository.findByUserId(viewerUserId);
      if (student.isPresent()) studentId = student.get().getId();
    }

    List<LessonResponseDto> lessonList = new ArrayList<>();
    for (Lesson lesson : lessonEntities) {
      LessonResponseDto dto = lessonMapper.toDto(lesson);
      if (studentId != null) {
        boolean isCompleted =
            lessonCompletionRepository.existsByStudentIdAndLessonIdAndStatus(
                studentId, lesson.getId(), LessonProgressStatus.COMPLETED);
        dto.setCompleted(isCompleted);
      } else {
        dto.setCompleted(false);
      }
      lessonList.add(dto);
    }

    int totalLessons = lessonEntities.size();
    int completedLessons = (int) lessonList.stream().filter(LessonResponseDto::isCompleted).count();

    Double avgRating = reviewRepository.avgRatingByCourseId(course.getId());
    if (avgRating == null) avgRating = 0.0;

    // QUAN TRỌNG: nên trả thêm instructorUserId cho FE (xem phần B)
    return new CourseResponseDto(
        course.getId(),
        course.getInstructor().getUser().getId(),
        course.getInstructor().getUser().getFullname(),
        course.getCategory().getName(),
        course.getPrice(),
        course.getCreatedAt(),
        course.getUpdatedAt(),
        course.getThumbnail(),
        course.getStatus(),
        course.getName(),
        course.getCategory().getId(),
        course.getRejectedReason(),
        lessonList,
        completedLessons,
        totalLessons,
        avgRating);
  }

  @Override
  // Return list of student with metadata about paging
  public PageDto getStudentOfCourse(Long id, int page, int limit) {
    if (!courseRepository.existsById(id)) {
      throw new NoSuchElementException("Course with id " + id + " is not found");
    }
    Page<Enroll> enrollPage = enrollRepository.findByCourseId(id, PageRequest.of(page, limit));
    List<StudentResponseDto> studentList =
        enrollPage.getContent().stream()
            .map(Enroll::getStudent)
            .map(studentMapper::toResponseDto)
            .toList();
    return new PageDto(
        enrollPage.getNumber(),
        enrollPage.getSize(),
        enrollPage.getTotalPages(),
        enrollPage.getTotalElements(),
        new ArrayList<>(studentList));
  }

  @Override
  // Return list of reviews with metadata about paging
  public PageDto getReviewOfCourse(Long id, int page, int limit) {
    if (!courseRepository.existsById(id)) {
      throw new NoSuchElementException("Course with id " + id + " is not found");
    }

    Page<Review> reviewPage = reviewRepository.findByCourseId(id, PageRequest.of(page, limit));
    List<ReviewResponseDto> reviewList =
        reviewPage.getContent().stream().map(reviewMapper::toResponseDTO).toList();
    return new PageDto(
        reviewPage.getNumber(),
        reviewPage.getSize(),
        reviewPage.getTotalPages(),
        reviewPage.getTotalElements(),
        new ArrayList<>(reviewList));
  }

  public static Specification<Course> courseFilter(
      String courseName, String categoryName, Double price, Long instructorId) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (courseName != null) {
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), "%" + courseName.toLowerCase() + "%"));
      }

      if (categoryName != null) {
        Join<Course, Category> categoryJoin = root.join("category");
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(categoryJoin.get("name")),
                "%" + categoryName.toLowerCase() + "%"));
      }

      if (price != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), price));
      }
      if (instructorId != null) {
        Join<Course, Instructor> instructorJoin = root.join("instructor");
        predicates.add(criteriaBuilder.equal(instructorJoin.get("id"), instructorId));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  @Transactional
  @Override
  public CourseResponseDto updateCourseStatus(Long courseId, String status, String rejectedReason) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(
                () -> new IllegalArgumentException("Course not found with id: " + courseId));

    String oldStatus = course.getStatus();
    String newStatus;

    if ("APPROVED".equalsIgnoreCase(status)) {
      newStatus = "APPROVED";
      course.setStatus(newStatus);
      course.setRejectedReason(null); // approved thì xoá reason
    } else if ("REJECTED".equalsIgnoreCase(status)) {
      newStatus = "REJECTED";
      course.setStatus(newStatus);

      // reject thì phải có lý do (nếu bạn muốn bắt buộc)
      if (rejectedReason == null || rejectedReason.isBlank()) {
        throw new IllegalArgumentException("rejectedReason is required when status=REJECTED");
      }
      course.setRejectedReason(rejectedReason.trim());
    } else {
      throw new IllegalArgumentException(
          "Invalid status value: " + status + " (must be either 'APPROVED' or 'REJECTED')");
    }

    courseRepository.save(course);

    // ===== Publish cho Instructor (kết quả duyệt) =====
    Long instructorUserId =
        course.getInstructor() != null && course.getInstructor().getUser() != null
            ? course.getInstructor().getUser().getId()
            : null;

    if (instructorUserId != null) {
      publisher.publishEvent(
          new InstructorEvents.CourseReviewOutcomeEvent(
              course.getId(), instructorUserId, newStatus));
    }

    // ===== Publish cho Student (khoá đổi trạng thái) =====
    publisher.publishEvent(
        new StudentEvents.CourseStatusChangedEvent(
            course.getId(), oldStatus == null ? "UNKNOWN" : oldStatus, newStatus));

    return courseMapper.toResponseDTO(course);
  }

  @Transactional
  @Override
  public CourseResponseDto resubmitCourse(Long courseId, Long instructorUserId) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(
                () -> new IllegalArgumentException("Course not found with id: " + courseId));

    // ownership check
    Long ownerUserId =
        course.getInstructor() != null && course.getInstructor().getUser() != null
            ? course.getInstructor().getUser().getId()
            : null;

    if (ownerUserId == null || !ownerUserId.equals(instructorUserId)) {
      throw new org.springframework.security.access.AccessDeniedException(
          "You can't resubmit others' course.");
    }

    if (!"REJECTED".equalsIgnoreCase(course.getStatus())) {
      throw new IllegalArgumentException("Only rejected course can be resubmitted.");
    }

    course.setStatus("PENDING");
    course.setRejectedReason(null);
    courseRepository.save(course);

    // notify Admin: course submitted for review
    publisher.publishEvent(
        new com.example.learning_management_system_api.events.AdminEvents
            .CourseSubmittedForReviewEvent(course.getId(), instructorUserId));

    return courseMapper.toResponseDTO(course);
  }
}
