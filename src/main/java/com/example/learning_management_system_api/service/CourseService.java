package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.mapper.ReviewMapper;
import com.example.learning_management_system_api.dto.mapper.StudentMapper;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.LessonResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ReviewResponseDto;
import com.example.learning_management_system_api.dto.response.StudentResponseDto;
import com.example.learning_management_system_api.entity.*;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.EnrollRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.utils.enums.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CourseService implements ICourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final StudentMapper studentMapper;
  private final EnrollRepository enrollRepository;
  private final ReviewRepository reviewRepository;
  private final ReviewMapper reviewMapper;
  private final InstructorRepository instructorRepository;
  private final LessonService lessonService;

  public CourseService(
      CourseRepository courseRepository,
      CourseMapper courseMapper,
      StudentMapper studentMapper,
      EnrollRepository enrollRepository,
      ReviewRepository reviewRepository,
      ReviewMapper reviewMapper,
      InstructorRepository instructorRepository,
      LessonService lessonService) {
    this.instructorRepository = instructorRepository;
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.studentMapper = studentMapper;
    this.enrollRepository = enrollRepository;
    this.reviewRepository = reviewRepository;
    this.reviewMapper = reviewMapper;
    this.lessonService = lessonService;
  }

  // Return list of course with metadata about paging
  public PageDto getAllCourse(
      int page,
      int limit,
      String courseName,
      String categoryName,
      Double price,
      Long instructorId,
      CustomUserDetails userDetails) // Đổi tham số cuối!
      {
    User user = userDetails.getUser(); // Lấy thực thể User từ CustomUserDetails

    UserRole role = user.getRole(); // hoặc user.getRole().name() nếu là Enum
    Long userId = user.getId();

    Specification<Course> spec = courseFilter(courseName, categoryName, price, instructorId);

    if (role == UserRole.Student) {
      spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), "APPROVED"));
    } else if (role == UserRole.Instructor) {
      // Đổi userId sang instructorId
      Instructor instructor = instructorRepository.findByUserId(userId);
      if (instructor == null)
        throw new RuntimeException("Instructor not found for userId: " + userId);
      spec =
          spec.and(
              (root, query, cb) -> cb.equal(root.get("instructor").get("id"), instructor.getId()));
    }

    // Admin thì không cần filter thêm

    Page<Course> coursePage = courseRepository.findAll(spec, PageRequest.of(page, limit));

    List<CourseResponseDto> result =
        coursePage.getContent().stream().map(courseMapper::toResponseDTO).toList();
    return new PageDto(
        coursePage.getNumber(),
        coursePage.getSize(),
        coursePage.getTotalPages(),
        coursePage.getTotalElements(),
        new ArrayList<>(result));
  }

  @SneakyThrows
  public CourseResponseDto getCourse(Long id) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(
                () -> new NoSuchElementException("Course with id " + id + " is not found"));
    // Lấy danh sách bài học (KHÔNG check quyền)
    List<LessonResponseDto> lessonList = lessonService.getAllLessons(course.getId(), null);

    return new CourseResponseDto(
        course.getId(),
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
        lessonList);
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

  @Override
  public CourseResponseDto updateCourseStatus(Long courseId, String status) {
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(
                () -> new IllegalArgumentException("Course not found with id: " + courseId));

    if ("APPROVED".equalsIgnoreCase(status)) {
      course.setStatus("APPROVED");
    } else if ("REJECTED".equalsIgnoreCase(status)) {
      course.setStatus("REJECTED");
    } else {
      throw new IllegalArgumentException(
          "Invalid status value: " + status + " (must be either 'APPROVED' or 'REJECTED')");
    }

    courseRepository.save(course);

    return courseMapper.toResponseDTO(course);
  }
}
