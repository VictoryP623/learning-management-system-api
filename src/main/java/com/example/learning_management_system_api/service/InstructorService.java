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
import com.example.learning_management_system_api.repository.CategoryRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.repository.WithdrawRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

  public InstructorService(
      CourseRepository courseRepository,
      CourseMapper courseMapper,
      WithdrawRepository withdrawRepository,
      InstructorRepository instructorRepository,
      CategoryRepository categoryRepository,
      LessonService lessonService,
      WithdrawMapper withdrawMapper,
      ReviewRepository reviewRepository) {
    this.reviewRepository = reviewRepository;
    this.courseRepository = courseRepository;
    this.courseMapper = courseMapper;
    this.withdrawRepository = withdrawRepository;
    this.instructorRepository = instructorRepository;
    this.categoryRepository = categoryRepository;
    this.lessonService = lessonService;
    this.withdrawMapper = withdrawMapper;
  }

  public CourseResponseDto createCourse(CourseDTO courseDTO) {
    Course courseResult = null;

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();
      Course course = new Course(courseDTO);
      course.setInstructor(instructorRepository.findByUserId(userId));
      course.setStatus("PENDING");
      course.setCategory(categoryRepository.findById(courseDTO.getCategoryId()).get());
      courseResult = courseRepository.save(course);
    }

    return courseMapper.toResponseDTO(courseResult);
  }

  // Need Instructor, Category service to function properly
  public CourseResponseDto updateCourse(CourseDTO courseDTO) {
    Course courseResult = null;
    Course courseCheck =
        courseRepository
            .findById(courseDTO.getId())
            .orElseThrow(() -> new NoSuchElementException("Not found course"));
    lessonService.checkPermission(courseCheck);
    courseRepository
        .findById(courseDTO.getId())
        .orElseThrow(() -> new NoSuchElementException("Not found course"));
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();
      Course course = new Course(courseDTO);
      course.setInstructor(instructorRepository.findByUserId(userId));
      course.setStatus("PENDING");
      course.setCreatedAt(courseCheck.getCreatedAt());
      course.setCategory(
          categoryRepository
              .findById(courseDTO.getCategoryId())
              .orElseThrow(() -> new NoSuchElementException("Not found category")));
      courseResult = courseRepository.save(course);
    }
    return courseMapper.toResponseDTO(courseResult);
  }

  public String deleteCourse(Long id) {
    Course courseResult = null;
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
    // Lấy instructor
    Instructor instructor =
        instructorRepository
            .findById(instructorId)
            .orElseThrow(() -> new NoSuchElementException("Instructor not found"));

    // Lấy danh sách course theo instructor
    Page<Course> coursePage =
        courseRepository.findByInstructorId(instructorId, PageRequest.of(page, limit));

    List<CourseResponseDto> coursesDTOList =
        coursePage.getContent().stream()
            .map(
                course -> {
                  // Gọi mapper để map các trường bình thường
                  CourseResponseDto dto = courseMapper.toResponseDTO(course);

                  // Tính trung bình rating
                  Double rating = reviewRepository.avgRatingByCourseId(course.getId());
                  rating = rating != null ? Math.round(rating * 10) / 10.0 : 0.0;

                  // Trả về một CourseResponseDto mới, giữ các trường cũ và thay đổi trường rating
                  return new CourseResponseDto(
                      dto.id(),
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
