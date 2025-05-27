package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.mapper.CourseMapper;
import com.example.learning_management_system_api.dto.response.CourseResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.PurchaseResponseDto;
import com.example.learning_management_system_api.entity.Cart;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Enroll;
import com.example.learning_management_system_api.entity.Id.EnrollId;
import com.example.learning_management_system_api.entity.Purchase;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.CartRepository;
import com.example.learning_management_system_api.repository.EnrollRepository;
import com.example.learning_management_system_api.repository.LessonCompletionRepository;
import com.example.learning_management_system_api.repository.LessonRepository;
import com.example.learning_management_system_api.repository.PurchaseRepository;
import com.example.learning_management_system_api.repository.ReviewRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

  @Value("${paypal.client.id}")
  private String clientId;

  @Value("${paypal.client.secret}")
  private String clientSecret;

  @Value("${paypal.mode}")
  private String mode;

  @Value("${paypal.return.url}")
  private String returnUrl;

  @Value("${paypal.cancel.url}")
  private String cancelUrl;

  private final PurchaseRepository purchaseRepository;
  private final CartRepository cartRepository;
  private final StudentRepository studentRepository;
  private final CourseMapper courseMapper;
  private final LessonCompletionRepository lessonCompletionRepository;
  private final LessonRepository lessonRepository;
  private final EnrollRepository enrollRepository;
  private final ReviewRepository reviewRepository;

  public PurchaseService(
      PurchaseRepository purchaseRepository,
      CartRepository cartRepository,
      StudentRepository studentRepository,
      CourseMapper courseMapper,
      LessonCompletionRepository lessonCompletionRepository,
      LessonRepository lessonRepository,
      EnrollRepository enrollRepository,
      ReviewRepository reviewRepository) {
    this.reviewRepository = reviewRepository;
    this.enrollRepository = enrollRepository;
    this.lessonCompletionRepository = lessonCompletionRepository;
    this.lessonRepository = lessonRepository;
    this.purchaseRepository = purchaseRepository;
    this.cartRepository = cartRepository;
    this.studentRepository = studentRepository;
    this.courseMapper = courseMapper;
  }

  // Tạo order PayPal
  public Map<String, String> createPaypalPayment(Long userId, List<Long> courseIds) {
    Optional<Student> studentOpt = studentRepository.findByUserId(userId);
    if (studentOpt.isEmpty()) throw new NoSuchElementException("UserId not found or not a student");
    Student student = studentOpt.get();

    // Chỉ lấy cart chứa courseId được chọn
    List<Cart> cartList =
        cartRepository.findByStudent(student).stream()
            .filter(c -> courseIds.contains(c.getCourse().getId()))
            .toList();
    List<Course> courseList = cartList.stream().map(Cart::getCourse).toList();
    if (courseList.isEmpty()) throw new AppException(400, "Không có khóa học nào được chọn!");

    Set<Course> courseSet = new HashSet<>(courseList);
    Double totalAmount = courseList.stream().mapToDouble(Course::getPrice).sum();

    // Lưu purchase với trạng thái chưa thanh toán
    Purchase purchase = new Purchase();
    purchase.setStudent(student);
    purchase.setIsPaid(false);
    purchase.setCourses(courseSet);
    purchase.setTotalAmount(totalAmount);
    Purchase savedPurchase = purchaseRepository.save(purchase);

    // Tạo order PayPal như cũ
    Amount amount = new Amount();
    amount.setCurrency("USD");
    amount.setTotal(String.format("%.2f", totalAmount));
    Transaction transaction = new Transaction();
    transaction.setDescription("Thanh toán khóa học #" + savedPurchase.getId());
    transaction.setAmount(amount);

    List<Transaction> transactions = new ArrayList<>();
    transactions.add(transaction);

    Payer payer = new Payer();
    payer.setPaymentMethod("paypal");

    Payment payment = new Payment();
    payment.setIntent("sale");
    payment.setPayer(payer);
    payment.setTransactions(transactions);

    RedirectUrls redirectUrls = new RedirectUrls();
    redirectUrls.setCancelUrl(cancelUrl);
    redirectUrls.setReturnUrl(returnUrl + "?purchaseId=" + savedPurchase.getId());
    payment.setRedirectUrls(redirectUrls);

    try {
      APIContext apiContext = new APIContext(clientId, clientSecret, mode);
      Payment createdPayment = payment.create(apiContext);

      String approvalLink =
          createdPayment.getLinks().stream()
              .filter(link -> "approval_url".equals(link.getRel()))
              .findFirst()
              .map(Links::getHref)
              .orElseThrow(() -> new RuntimeException("No approval_url found in PayPal response"));

      Map<String, String> result = new HashMap<>();
      result.put("payUrl", approvalLink);
      return result;
    } catch (PayPalRESTException e) {
      throw new RuntimeException("PayPal error: " + e.getMessage(), e);
    }
  }

  // Xác nhận thanh toán khi PayPal redirect về
  @Transactional
  public void executePaypalPayment(String paymentId, String payerId, Long purchaseId)
      throws PayPalRESTException {

    APIContext apiContext = new APIContext(clientId, clientSecret, mode);
    Payment payment = new Payment();
    payment.setId(paymentId);

    PaymentExecution paymentExecution = new PaymentExecution();
    paymentExecution.setPayerId(payerId);

    Payment executedPayment = payment.execute(apiContext, paymentExecution);

    // Nếu payment thành công, update is_paid và xóa cart
    if ("approved".equalsIgnoreCase(executedPayment.getState())) {
      Purchase purchase =
          purchaseRepository
              .findById(purchaseId)
              .orElseThrow(() -> new RuntimeException("Purchase not found"));
      purchase.setIsPaid(true);
      purchaseRepository.save(purchase);

      // Xóa cart của student này sau khi xác nhận thành công
      Student student = purchase.getStudent();
      for (Course course : purchase.getCourses()) {
        cartRepository.deleteByStudentAndCourse(student, course);
      }

      // ===== Thêm logic ENROLL sau khi mua thành công =====
      for (Course course : purchase.getCourses()) {
        EnrollId enrollId = new EnrollId(student.getId(), course.getId());
        if (!enrollRepository.existsById(enrollId)) {
          Enroll enroll = new Enroll();
          enroll.setId(enrollId);
          enroll.setStudent(student);
          enroll.setCourse(course);
          enrollRepository.save(enroll);
        }
      }
      // ====================================================
    } else {
      throw new RuntimeException("Payment not approved");
    }
  }

  public List<PurchaseResponseDto> getAllPurchase() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {
      List<Purchase> listPurchase =
          purchaseRepository.findDistinctCoursesByStudent_User_IdAndIsPaidTrue(
              customUserDetails.getUserId());
      return listPurchase.stream().map(this::toDto).toList();
    } else return null;
  }

  public PageDto getBoughtCourse(int page, int size) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.getPrincipal() instanceof CustomUserDetails customUserDetails) {

      // Lấy studentId hiện tại
      Long userId = customUserDetails.getUserId();
      Optional<Student> studentOpt = studentRepository.findByUserId(userId);
      Long studentId = studentOpt.map(Student::getId).orElse(null);

      // Lấy các purchase đã thanh toán
      List<Purchase> purchaseList =
          purchaseRepository.findDistinctCoursesByStudent_User_IdAndIsPaidTrue(userId);

      // Lấy các khóa học đã mua (không trùng)
      List<Course> boughtCourseEntity =
          purchaseList.stream()
              .flatMap(purchase -> purchase.getCourses().stream())
              .distinct()
              .toList();

      // Map từng course, tính completed/total
      List<CourseResponseDto> boughtCourse =
          boughtCourseEntity.stream()
              .map(
                  course -> {
                    int totalLessons = (int) lessonRepository.countByCourseId(course.getId());
                    int completedLessons = 0;
                    if (studentId != null) {
                      completedLessons =
                          (int)
                              lessonCompletionRepository.countByStudentIdAndLesson_Course_Id(
                                  studentId, course.getId());
                    }
                    // Tính rating trung bình của course (nếu có cột này hoặc phải join từ bảng
                    // review)
                    Double avgRating = reviewRepository.avgRatingByCourseId(course.getId());
                    // Nếu không có, truyền 0.0 hoặc null tùy trường hợp
                    if (avgRating == null) avgRating = 0.0;

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
                        null, // lessons
                        completedLessons,
                        totalLessons,
                        avgRating // <-- Bắt buộc truyền trường này
                        );
                  })
              .toList();

      // Paging thủ công
      if (page < 0 || size < 1) {
        throw new IllegalArgumentException("Page must start from 0 and size must greater than 0");
      }
      int totalElements = boughtCourse.size();
      int totalPages = (int) Math.ceil((double) totalElements / size);
      int startIndex = page * size;
      int endIndex = Math.min(startIndex + size, totalElements);

      List<CourseResponseDto> pagedCourse = new ArrayList<>();
      if (startIndex < totalElements) {
        pagedCourse = boughtCourse.subList(startIndex, endIndex);
      }

      return new PageDto(page, size, totalPages, totalElements, new ArrayList<Object>(pagedCourse));

    } else {
      return null;
    }
  }

  PurchaseResponseDto toDto(Purchase purchase) {
    List<CourseResponseDto> listCourse =
        purchase.getCourses().stream().map(courseMapper::toResponseDTO).toList();
    return new PurchaseResponseDto(
        purchase.getId(), purchase.getTotalAmount(), purchase.getCreatedAt(), listCourse);
  }
}
