package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.ReportDTO;
import com.example.learning_management_system_api.dto.ReviewDTO;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.mapper.InstructorMapper;
import com.example.learning_management_system_api.dto.mapper.ReportMapper;
import com.example.learning_management_system_api.dto.mapper.ReviewMapper;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.InstructorResponseDTO;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Cart;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Enroll;
import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.Id.CartId;
import com.example.learning_management_system_api.entity.Id.EnrollId;
import com.example.learning_management_system_api.entity.Id.ReportId;
import com.example.learning_management_system_api.entity.Id.ReviewId;
import com.example.learning_management_system_api.entity.Instructor;
import com.example.learning_management_system_api.entity.Report;
import com.example.learning_management_system_api.entity.Review;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.exception.ConflictException;
import com.example.learning_management_system_api.exception.NotFoundException;
import com.example.learning_management_system_api.repository.CartRepository;
import com.example.learning_management_system_api.repository.CourseRepository;
import com.example.learning_management_system_api.repository.EnrollRepository;
import com.example.learning_management_system_api.repository.FollowRepository;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.ReportRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class StudentService implements IStudentService {
  @Autowired private StudentRepository studentRepository;

  @Autowired private ReviewRepository reviewRepository;

  @Autowired private InstructorRepository instructorRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private EnrollRepository enrollRepository;

  @Autowired private FollowRepository followRepository;

  @Autowired private ReportRepository reportRepository;

  @Autowired private CartRepository cartRepository;

  @Autowired private InstructorMapper instructorMapper;

  @Autowired private ReviewMapper reviewMapper;

  @Autowired private ReportMapper reportMapper;

  @Autowired private CourseMapper courseMapper;

  @Autowired private LessonService lessonService;

  @Override
  public Optional<Student> getStudentById(Long id) {
    return studentRepository.findByUserId(id);
  }

  // UC10: Xem khóa học đã đăng ký
  @Override
  public PageDto getEnrolledCourses(Long studentId, int page, int limit) {
    Optional<Page<Enroll>> optionalEnrollPage =
        enrollRepository.findByStudentId(studentId, PageRequest.of(page, limit));

    if (optionalEnrollPage.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    Page<Enroll> enrollPage = optionalEnrollPage.get();

    List<CourseResponseDto> coursesDTOList =
        enrollPage.getContent().stream()
            .map(enroll -> courseMapper.toResponseDTO(enroll.getCourse()))
            .toList();

    return new PageDto(
        enrollPage.getNumber(),
        enrollPage.getSize(),
        enrollPage.getTotalPages(),
        enrollPage.getTotalElements(),
        new ArrayList<>(coursesDTOList));
  }

  // UC12: Xem giảng viên đã theo dõi
  @Override
  public PageDto getFollows(Long studentId, int page, int limit) {
    Optional<Page<Follow>> optionalFollowPage =
        followRepository.findByStudentId(studentId, PageRequest.of(page, limit));

    if (optionalFollowPage.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    Page<Follow> followPage = optionalFollowPage.get();

    List<InstructorResponseDTO> instructorsDTOList =
        followPage.getContent().stream()
            .map(follow -> instructorMapper.toResponseDto(follow.getInstructor()))
            .toList();

    return new PageDto(
        followPage.getNumber(),
        followPage.getSize(),
        followPage.getTotalPages(),
        followPage.getTotalElements(),
        new ArrayList<>(instructorsDTOList));
  }

  // UC15: Đăng ký khóa học
  @Override
  public String enrollCourse(EnrollId enrollId) {
    // Kiểm tra nếu studentId không hợp lệ
    Optional<Student> savedStudent = studentRepository.findById(enrollId.getStudentId());
    if (savedStudent.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    // Kiểm tra nếu courseId không hợp lệ
    Optional<Course> savedCourse = courseRepository.findById(enrollId.getCourseId());
    if (savedCourse.isEmpty()) {
      throw new IllegalArgumentException("Invalid course ID");
    }

    // Kiểm tra nếu enrollment đã tồn tại
    if (enrollRepository.existsById(enrollId)) {
      throw new ConflictException("Student is already enrolled in the course");
    }

    // Kiểm tra nếu người dùng đã mua khóa học chưa
    Course course = savedCourse.get();
    lessonService.checkGetPermission(course); // Gọi hàm check quyền từ LessonService

    // Tạo enrollment mới
    Enroll enroll = new Enroll();
    enroll.setId(enrollId);
    enroll.setStudent(savedStudent.get());
    enroll.setCourse(savedCourse.get());

    enrollRepository.save(enroll);
    return "Enrollment successful";
  }

  // UC16: Đánh giá khóa học
  @Override
  public ReviewDTO submitReview(ReviewDTO reviewDTO) {
    ReviewId reviewId = reviewDTO.id();

    // Kiểm tra nếu review đã tồn tại
    if (reviewRepository.existsById(reviewId)) {
      throw new ConflictException("Review already exists");
    }

    // Xác nhận sự tồn tại của student
    Optional<Student> student = studentRepository.findById(reviewId.getStudentId());
    if (student.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    // Xác nhận sự tồn tại của course
    Optional<Course> course = courseRepository.findById(reviewId.getCourseId());
    if (course.isEmpty()) {
      throw new IllegalArgumentException("Invalid course ID");
    }

    // Kiểm tra nếu student đã đăng ký khóa học
    if (!enrollRepository.existsById(
        new EnrollId(reviewId.getStudentId(), reviewId.getCourseId()))) {
      throw new NotFoundException("Student is not enrolled in the course");
    }

    // Tạo mới review
    Review review = reviewMapper.toEntity(reviewDTO);
    review.setStudent(student.get());
    review.setCourse(course.get());

    Review savedReview = reviewRepository.save(review);

    return reviewMapper.toDto(savedReview);
  }

  @Override
  public ReviewDTO updateReview(ReviewDTO reviewDTO) {
    ReviewId reviewId = reviewDTO.id();

    // Kiểm tra nếu review không tồn tại
    Optional<Review> optionalReview = reviewRepository.findById(reviewId);
    if (optionalReview.isEmpty()) {
      throw new IllegalArgumentException(
          "Review does not exist for the provided student ID and course ID");
    }

    // Cập nhật các trường cần thiết
    Review existingReview = optionalReview.get();
    if (reviewDTO.description() != null) {
      existingReview.setDescription(reviewDTO.description());
    }

    // Lưu thay đổi
    Review updatedReview = reviewRepository.save(existingReview);

    // Trả về DTO
    return reviewMapper.toDto(updatedReview);
  }

  @Override
  public ReviewDTO getReview(ReviewId reviewId) {
    Optional<Review> optionalReview = reviewRepository.findById(reviewId);

    if (optionalReview.isEmpty()) {
      throw new NotFoundException(
          "Review does not exist for the provided student ID and course ID");
    }

    return reviewMapper.toDto(optionalReview.get());
  }

  @Override
  public List<ReviewDTO> getAllReviewsByCourseId(Long courseId) {
    List<Review> reviews = reviewRepository.findByCourseId(courseId);
    return reviews.stream().map(reviewMapper::toDto).toList();
  }

  @Override
  public String deleteReview(ReviewId reviewId) {
    if (!reviewRepository.existsById(reviewId)) {
      throw new NotFoundException(
          "Review does not exist for the provided student ID and course ID");
    }

    reviewRepository.deleteById(reviewId);

    return "Review deleted successfully";
  }

  // UC13: Tìm kiếm giảng viên theo tên
  @Override
  public PageDto searchInstructors(String name, int page, int limit) {
    Page<Instructor> instructorPage =
        instructorRepository.findByUserFullnameContainingIgnoreCase(
            name, PageRequest.of(page, limit));

    List<InstructorResponseDTO> instructorsDTOPage =
        instructorPage.getContent().stream().map(instructorMapper::toResponseDto).toList();

    return new PageDto(
        instructorPage.getNumber(),
        instructorPage.getSize(),
        instructorPage.getTotalPages(),
        instructorPage.getTotalElements(),
        new ArrayList<>(instructorsDTOPage));
  }

  // UC14: Thêm khóa học vào giỏ hàng
  @Override
  public String addToCart(CartId cartId) {
    // Tạo mới đối tượng Cart
    Cart cart = new Cart();
    cart.setId(cartId);

    // Kiểm tra nếu cart đã tồn tại
    if (cartRepository.existsById(cartId)) {
      throw new ConflictException("The course is already in the cart");
    }

    // Kiểm tra và lấy thông tin Student
    Optional<Student> savedStudent = studentRepository.findById(cart.getId().getStudentId());
    if (savedStudent.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }
    cart.setStudent(savedStudent.get());

    // Kiểm tra và lấy thông tin Course
    Optional<Course> savedCourse = courseRepository.findById(cart.getId().getCourseId());
    if (savedCourse.isEmpty()) {
      throw new IllegalArgumentException("Invalid course ID");
    }
    cart.setCourse(savedCourse.get());

    // Lưu cart mới vào database
    cartRepository.save(cart);

    return "Course added to cart successfully";
  }

  // Lấy tất cả các khóa học trong giỏ hàng
  @Override
  public PageDto getAllInCart(Long studentId, int page, int limit) {
    Optional<Student> student = studentRepository.findById(studentId);
    if (student.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    Page<Cart> cartPage = cartRepository.findByStudentId(studentId, PageRequest.of(page, limit));
    List<CourseResponseDto> coursesDTOList =
        cartPage.getContent().stream()
            .map(cart -> courseMapper.toResponseDTO(cart.getCourse()))
            .toList();

    return new PageDto(
        cartPage.getNumber(),
        cartPage.getSize(),
        cartPage.getTotalPages(),
        cartPage.getTotalElements(),
        new ArrayList<>(coursesDTOList));
  }

  // Xóa mục trong giỏ hàng
  @Override
  public String deleteItemInCart(CartId cartId) {
    // Kiểm tra nếu mục trong giỏ hàng không tồn tại
    if (!cartRepository.existsById(cartId)) {
      throw new NotFoundException("Item not found in cart");
    }

    // Xóa mục trong giỏ hàng
    cartRepository.deleteById(cartId);
    return "Item removed from cart successfully";
  }

  // UC17: Xử lý báo cáo khóa học
  @Override
  public ReportDTO submitReport(ReportDTO reportDTO) {
    ReportId reportId = reportDTO.id();

    // Kiểm tra nếu báo cáo đã tồn tại
    if (reportRepository.existsById(reportId)) {
      throw new ConflictException("Report already exists");
    }

    // Kiểm tra sự tồn tại của student
    Optional<Student> student = studentRepository.findById(reportId.getStudentId());
    if (student.isEmpty()) {
      throw new IllegalArgumentException("Invalid student ID");
    }

    // Kiểm tra sự tồn tại của course
    Optional<Course> course = courseRepository.findById(reportId.getCourseId());
    if (course.isEmpty()) {
      throw new IllegalArgumentException("Invalid course ID");
    }

    // Kiểm tra nếu student đã đăng ký khóa học
    if (!enrollRepository.existsById(
        new EnrollId(reportId.getStudentId(), reportId.getCourseId()))) {
      throw new NotFoundException("Student is not enrolled in the course");
    }

    // Tạo mới báo cáo
    Report report = reportMapper.toEntity(reportDTO);
    report.setStudent(student.get());
    report.setCourse(course.get());

    Report savedReport = reportRepository.save(report);
    return reportMapper.toDto(savedReport);
  }

  @Override
  public ReportDTO updateReport(ReportDTO reportDTO) {
    ReportId reportId = reportDTO.id();

    // Kiểm tra nếu report không tồn tại
    Optional<Report> optionalReport = reportRepository.findById(reportId);
    if (optionalReport.isEmpty()) {
      throw new IllegalArgumentException(
          "Report does not exist for the provided student ID and course ID");
    }

    // Cập nhật các trường cần thiết
    Report existingReport = optionalReport.get();
    if (reportDTO.description() != null) {
      existingReport.setDescription(reportDTO.description());
    }

    // Lưu thay đổi
    Report updatedReport = reportRepository.save(existingReport);

    // Trả về DTO
    return reportMapper.toDto(updatedReport);
  }

  @Override
  public ReportDTO getReport(ReportId reportId) {
    Optional<Report> optionalReport = reportRepository.findById(reportId);

    if (optionalReport.isEmpty()) {
      throw new NotFoundException(
          "Report does not exist for the provided student ID and course ID");
    }

    return reportMapper.toDto(optionalReport.get());
  }

  @Override
  public List<ReportDTO> getAllReportsByCourseId(Long courseId) {
    List<Report> reports = reportRepository.findByCourseId(courseId);
    return reports.stream().map(reportMapper::toDto).toList();
  }

  @Override
  public String deleteReport(ReportId reportId) {
    if (!reportRepository.existsById(reportId)) {
      throw new NotFoundException(
          "Report does not exist for the provided student ID and course ID");
    }

    reportRepository.deleteById(reportId);

    return "Report deleted successfully";
  }
}
