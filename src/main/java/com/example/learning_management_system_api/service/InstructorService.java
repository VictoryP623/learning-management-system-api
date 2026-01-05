package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.mapper.WithdrawMapper;
import com.example.learning_management_system_api.dto.request.CourseDTO;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.EarningDTO;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.WithdrawResponseDTO;
import com.example.learning_management_system_api.entity.*;
import com.example.learning_management_system_api.events.AdminEvents;
import com.example.learning_management_system_api.repository.CategoryRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.repository.WithdrawRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstructorService {
  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;
  private final WithdrawRepository withdrawRepository;
  private final InstructorRepository instructorRepository;
  private final CategoryRepository categoryRepository;
  private final LessonService lessonService;
  private final WithdrawMapper withdrawMapper;
  private final ReviewRepository reviewRepository;

  // NEW
  private final ApplicationEventPublisher publisher;

  public InstructorService(
      CourseRepository courseRepository,
      CourseMapper courseMapper,
      WithdrawRepository withdrawRepository,
      InstructorRepository instructorRepository,
      CategoryRepository categoryRepository,
      LessonService lessonService,
      WithdrawMapper withdrawMapper,
      ReviewRepository reviewRepository,
      ApplicationEventPublisher publisher // NEW
      ) {
    this.reviewRepository = reviewRepository;
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.withdrawRepository = withdrawRepository;
    this.instructorRepository = instructorRepository;
    this.categoryRepository = categoryRepository;
    this.lessonService = lessonService;
    this.withdrawMapper = withdrawMapper;
    this.publisher = publisher; // NEW
  }

  @Transactional
  public CourseResponseDto createCourse(CourseDTO courseDTO) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
      throw new RuntimeException("Unauthorized");
    }

    Long userId = customUserDetails.getUserId();

    Instructor instructor = instructorRepository.findByUserId(userId);
    if (instructor == null) {
      throw new RuntimeException("Instructor not found for userId: " + userId);
    }

    Course course = new Course(courseDTO);
    course.setInstructor(instructor);
    course.setStatus("PENDING");
    course.setCategory(categoryRepository.findById(courseDTO.getCategoryId()).orElseThrow());
    Course saved = courseRepository.save(course);

    // NEW: notify admin course submitted
    try {
      publisher.publishEvent(
          new AdminEvents.CourseSubmittedForReviewEvent(saved.getId(), instructor.getId()));
    } catch (Exception ignore) {
    }

    return courseMapper.toResponseDTO(saved);
  }

  @Transactional
  public CourseResponseDto updateCourse(CourseDTO courseDTO) {
    Course courseCheck =
        courseRepository
            .findById(courseDTO.getId())
            .orElseThrow(() -> new NoSuchElementException("Not found course"));

    lessonService.checkPermission(courseCheck);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
      throw new RuntimeException("Unauthorized");
    }

    Long userId = customUserDetails.getUserId();
    Instructor instructor = instructorRepository.findByUserId(userId);
    if (instructor == null) {
      throw new RuntimeException("Instructor not found for userId: " + userId);
    }

    Course course = new Course(courseDTO);
    course.setInstructor(instructor);
    course.setStatus("PENDING");
    course.setCreatedAt(courseCheck.getCreatedAt());
    course.setCategory(
        categoryRepository
            .findById(courseDTO.getCategoryId())
            .orElseThrow(() -> new NoSuchElementException("Not found category")));

    Course saved = courseRepository.save(course);

    // NEW: update => resubmit for review
    try {
      publisher.publishEvent(
          new AdminEvents.CourseSubmittedForReviewEvent(saved.getId(), instructor.getId()));
    } catch (Exception ignore) {
    }

    return courseMapper.toResponseDTO(saved);
  }

  public String deleteCourse(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Course course =
          courseRepository
              .findById(id)
              .orElseThrow(() -> new NoSuchElementException("Not found course"));
      lessonService.checkPermission(course);
      courseRepository.deleteById(id);
      return "Course deleted successfully";
    }
    return "Not instructor";
  }

  public List<EarningDTO> getEarning(Long instructorId) {
    List<Object[]> rows = courseRepository.getEarningsNative(instructorId);
    List<EarningDTO> earningDTOs = new ArrayList<>();
    for (Object[] row : rows) {
      Long courseId = ((Number) row[0]).longValue();
      Long instructorIdVal = ((Number) row[1]).longValue();
      String courseName = (String) row[2];
      Integer soldCount = row[3] != null ? ((Number) row[3]).intValue() : 0;
      Double revenue = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
      earningDTOs.add(new EarningDTO(courseId, instructorIdVal, courseName, soldCount, revenue));
    }
    return earningDTOs;
  }

  public WithdrawResponseDTO withdraw(Long instructorId, Double amount) {
    WithdrawResponseDTO withdrawResponseDTO = null;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Withdraw withdraw = new Withdraw();
      withdraw.setInstructorId(instructorId);
      withdraw.setAmount(amount);
      Withdraw result = withdrawRepository.save(withdraw);
      withdrawResponseDTO = withdrawMapper.toResponseDTO(result);
    }
    return withdrawResponseDTO;
  }

  public PageDto getCoursesByInstructor(Long instructorId, int page, int limit) {
    instructorRepository
        .findById(instructorId)
        .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

    Page<Course> coursePage =
        courseRepository.findByInstructorId(instructorId, PageRequest.of(page, limit));

    List<CourseResponseDto> coursesDTOList =
        coursePage.getContent().stream()
            .map(
                course -> {
                  CourseResponseDto dto = courseMapper.toResponseDTO(course);
                  Double rating = reviewRepository.avgRatingByCourseId(course.getId());
                  rating = rating != null ? Math.round(rating * 10) / 10.0 : 0.0;
                  return new CourseResponseDto(
                      dto.id(),
                      dto.instructorUserId(),
                      dto.instructorName(),
                      dto.categoryName(),
                      dto.price(),
                      dto.createdAt(),
                      dto.updatedAt(),
                      dto.thumbnail(),
                      dto.status(),
                      dto.name(),
                      dto.categoryId(),
                      dto.rejectedReason(),
                      dto.lessons(),
                      dto.completedLessons(),
                      dto.totalLessons(),
                      rating);
                })
            .toList();

    return new PageDto(
        coursePage.getNumber(),
        coursePage.getSize(),
        coursePage.getTotalPages(),
        coursePage.getTotalElements(),
        new ArrayList<>(coursesDTOList));
  }
}
