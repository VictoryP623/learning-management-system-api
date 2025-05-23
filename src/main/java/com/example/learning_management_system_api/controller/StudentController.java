package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.ReportDTO;
import com.example.learning_management_system_api.dto.ReviewDTO;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.entity.Id.CartId;
import com.example.learning_management_system_api.entity.Id.EnrollId;
import com.example.learning_management_system_api.entity.Id.ReportId;
import com.example.learning_management_system_api.entity.Id.ReviewId;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.example.learning_management_system_api.service.IStudentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {
  @Autowired private IStudentService studentService;
  @Autowired private StudentRepository studentRepository;

  private Long getStudentIdFromAuthentication() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      Long userId = customUserDetails.getUserId();
      return studentRepository
          .findByUserId(userId)
          .map(Student::getId)
          .orElseThrow(() -> new IllegalStateException("Student not found for userId: " + userId));
    }
    throw new IllegalStateException("Student not authenticated");
  }

  // UC10: Xem khóa học đã đăng ký
  @GetMapping("/students/enrolled-courses")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<PageDto> getEnrolledCourses(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
    Long studentId = getStudentIdFromAuthentication();
    return ResponseVO.success(studentService.getEnrolledCourses(studentId, page, limit));
  }

  // UC12: Xem giảng viên đã theo dõi
  @GetMapping("/students/follows")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<PageDto> getFollows(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
    Long studentId = getStudentIdFromAuthentication();
    return ResponseVO.success(studentService.getFollows(studentId, page, limit));
  }

  // UC15: Đăng ký khóa học
  @PostMapping("/students/enroll/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<String> enrollCourse(@PathVariable Long courseId) {
    Long studentId = getStudentIdFromAuthentication();
    EnrollId enrollId = new EnrollId(studentId, courseId);
    return ResponseVO.success(studentService.enrollCourse(enrollId));
  }

  // UC16: Đánh giá khóa học
  @PostMapping("/students/reviews")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<ReviewDTO> submitReview(@RequestBody @Valid ReviewDTO reviewDTO) {
    Long studentId = getStudentIdFromAuthentication();
    ReviewDTO updatedReviewDTO =
        new ReviewDTO(
            new ReviewId(studentId, reviewDTO.id().getCourseId()),
            reviewDTO.description(),
            reviewDTO.createdAt(),
            reviewDTO.updatedAt());
    return ResponseVO.success(studentService.submitReview(updatedReviewDTO));
  }

  @PatchMapping("/students/reviews")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<ReviewDTO> updateReview(@RequestBody @Valid ReviewDTO reviewDTO) {
    Long studentId = getStudentIdFromAuthentication();
    ReviewDTO updatedReviewDTO =
        new ReviewDTO(
            new ReviewId(studentId, reviewDTO.id().getCourseId()),
            reviewDTO.description(),
            reviewDTO.createdAt(),
            reviewDTO.updatedAt());
    return ResponseVO.success(studentService.updateReview(updatedReviewDTO));
  }

  @GetMapping("/students/reviews/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseVO<List<ReviewDTO>> getAllReviewsByCourseId(@PathVariable Long courseId) {
    return ResponseVO.success(studentService.getAllReviewsByCourseId(courseId));
  }

  @DeleteMapping("/students/reviews/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<String> deleteReview(@PathVariable Long courseId) {
    Long studentId = getStudentIdFromAuthentication();
    ReviewId reviewId = new ReviewId(studentId, courseId);
    return ResponseVO.success(studentService.deleteReview(reviewId));
  }

  // UC13: Tìm kiếm giảng viên theo tên
  @GetMapping("/instructors")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseVO<PageDto> searchInstructors(
      @RequestParam String name,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int limit) {
    return ResponseVO.success(studentService.searchInstructors(name, page, limit));
  }

  // UC14: Thêm khóa học vào giỏ hàng
  @PostMapping("/students/carts/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<String> addToCart(@PathVariable Long courseId) {
    Long studentId = getStudentIdFromAuthentication();
    CartId cartId = new CartId(studentId, courseId);
    return ResponseVO.success(studentService.addToCart(cartId));
  }

  // Lấy tất cả các khóa học trong giỏ hàng
  @GetMapping("/students/carts")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<PageDto> getAllInCart(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
    Long studentId = getStudentIdFromAuthentication();
    return ResponseVO.success(studentService.getAllInCart(studentId, page, limit));
  }

  // Xóa mục trong giỏ hàng
  @DeleteMapping("/students/carts/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<String> deleteItemInCart(@PathVariable Long courseId) {
    Long studentId = getStudentIdFromAuthentication();
    CartId cartId = new CartId(studentId, courseId);
    return ResponseVO.success(studentService.deleteItemInCart(cartId));
  }

  // UC17: Gửi báo cáo khóa học
  @PostMapping("/students/reports")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<ReportDTO> submitReport(@RequestBody @Valid ReportDTO reportDTO) {
    Long studentId = getStudentIdFromAuthentication();
    ReportDTO updatedReportDTO =
        new ReportDTO(
            new ReportId(studentId, reportDTO.id().getCourseId()),
            reportDTO.description(),
            reportDTO.createdAt(),
            reportDTO.updatedAt());
    return ResponseVO.success(studentService.submitReport(updatedReportDTO));
  }

  @PatchMapping("/students/reports")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<ReportDTO> updateReport(@RequestBody @Valid ReportDTO reportDTO) {
    Long studentId = getStudentIdFromAuthentication();
    ReportDTO updatedReportDTO =
        new ReportDTO(
            new ReportId(studentId, reportDTO.id().getCourseId()),
            reportDTO.description(),
            reportDTO.createdAt(),
            reportDTO.updatedAt());
    return ResponseVO.success(studentService.updateReport(updatedReportDTO));
  }

  @GetMapping("/reports/{courseId}")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<List<ReportDTO>> getAllReportsByCourse(@PathVariable Long courseId) {
    return ResponseVO.success(studentService.getAllReportsByCourseId(courseId));
  }

  @DeleteMapping("/students/reports/{courseId}")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseVO<String> deleteReport(@PathVariable Long courseId) {
    Long studentId = getStudentIdFromAuthentication();
    ReportId reportId = new ReportId(studentId, courseId);
    return ResponseVO.success(studentService.deleteReport(reportId));
  }
}
