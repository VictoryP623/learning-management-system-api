package com.example.learning_management_system_api.notification;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.entity.Notification;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.events.AdminEvents.*;
import com.example.learning_management_system_api.events.InstructorEvents.*;
import com.example.learning_management_system_api.events.StudentEvents.*;
import com.example.learning_management_system_api.dto.mapper.NotificationMapper;
import com.example.learning_management_system_api.repository.*;
import com.example.learning_management_system_api.service.NotificationService;
import com.example.learning_management_system_api.utils.enums.NotificationTopic;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class DefaultDomainNotifier implements DomainNotifier {

  private final NotificationService notificationService;
  private final SimpMessagingTemplate messagingTemplate;
  private final EnrollRepository enrollRepo;
  private final CartRepository cartRepo;
  private final CourseRepository courseRepo;
  private final UserRepository userRepo;
  private final NotificationRepository notificationRepo;
  private final NotificationMapper notificationMapper;

  public DefaultDomainNotifier(
      NotificationService notificationService,
      SimpMessagingTemplate messagingTemplate,
      EnrollRepository enrollRepo,
      CartRepository cartRepo,
      CourseRepository courseRepo,
      UserRepository userRepo,
      NotificationRepository notificationRepo,
      NotificationMapper notificationMapper) {
    this.notificationService = notificationService;
    this.messagingTemplate = messagingTemplate;
    this.enrollRepo = enrollRepo;
    this.cartRepo = cartRepo;
    this.courseRepo = courseRepo;
    this.userRepo = userRepo;
    this.notificationRepo = notificationRepo;
    this.notificationMapper = notificationMapper;
  }

  /* ===== Helpers ===== */
  private boolean duplicated(String key) {
    return notificationRepo.existsByIdempotencyKey(key);
  }

  // KHÔNG set createdAt (đã có @CreationTimestamp trong entity)
  private Notification make(
      NotificationTopic topic,
      NotificationType type,
      String title,
      String body,
      String idem,
      String linkUrl,
      String dataJson) {
    return Notification.builder()
        .topic(topic)
        .type(type)
        .title(title)
        .message(body)
        .idempotencyKey(idem)
        .linkUrl(linkUrl)
        .dataJson(dataJson)
        .readFlag(false)
        .build();
  }

  // Lưu + đẩy DTO thống nhất lên /user/queue/notifications
  private void saveAndPush(Notification n, Long userId) {
    try {
      Notification saved = notificationService.save(n);
      NotificationResponse dto = notificationMapper.toDto(saved);
      messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", dto);
    } catch (DataIntegrityViolationException ignore) {
      // Trùng idempotency_key do race-condition -> bỏ qua yên lặng
    }
  }

  /* ===== Implementations with listeners ===== */

  // ===================== Student =====================

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseStatusChangedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    String link = "/courses/" + e.courseId();
    String data =
        "{\"courseId\":"
            + e.courseId()
            + ",\"from\":\""
            + e.fromStatus()
            + "\",\"to\":\""
            + e.toStatus()
            + "\"}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.COURSE_STATUS_CHANGED,
                  NotificationType.INFO,
                  "Khoá học cập nhật trạng thái",
                  "Khoá #" + e.courseId() + " chuyển " + e.fromStatus() + " → " + e.toStatus(),
                  e.idempotencyKey() + "#enroll:" + sid,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, sid);
    }

    for (Long uid : cartRepo.findUserIdsByCourseId(e.courseId())) {
      User u = userRepo.getReferenceById(uid);
      var n =
          make(
                  NotificationTopic.COURSE_STATUS_CHANGED,
                  NotificationType.INFO,
                  "Khoá học cập nhật trạng thái",
                  "(Theo dõi) Khoá #"
                      + e.courseId()
                      + " chuyển "
                      + e.fromStatus()
                      + " → "
                      + e.toStatus(),
                  e.idempotencyKey() + "#watch:" + uid,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, uid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(LessonCreatedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    String link = "/courses/" + e.courseId() + "/lessons/" + e.lessonId();
    String data = "{\"courseId\":" + e.courseId() + ",\"lessonId\":" + e.lessonId() + "}";
    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.LESSON_CREATED,
                  NotificationType.INFO,
                  "Bài học mới",
                  "Khoá #" + e.courseId() + " vừa thêm bài #" + e.lessonId(),
                  e.idempotencyKey() + "#" + sid,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, sid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(LessonUpdatedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    String link = "/courses/" + e.courseId() + "/lessons/" + e.lessonId();
    String data = "{\"courseId\":" + e.courseId() + ",\"lessonId\":" + e.lessonId() + "}";
    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.LESSON_UPDATED,
                  NotificationType.INFO,
                  "Bài học cập nhật",
                  "Bài #" + e.lessonId() + " của khoá #" + e.courseId() + " đã cập nhật.",
                  e.idempotencyKey() + "#" + sid,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, sid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(QuizPublishedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    String link = "/courses/" + e.courseId() + "/quizzes/" + e.quizId();
    String data = "{\"courseId\":" + e.courseId() + ",\"quizId\":" + e.quizId() + "}";
    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.QUIZ_PUBLISHED,
                  NotificationType.INFO,
                  "Quiz mới mở",
                  "Khoá #" + e.courseId() + " có quiz #" + e.quizId() + " vừa mở.",
                  e.idempotencyKey() + "#" + sid,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, sid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(StudyReminderEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User u = userRepo.getReferenceById(e.studentId());
    var n =
        make(
                NotificationTopic.STUDY_REMINDER,
                NotificationType.INFO,
                "Nhắc học",
                "Đã " + e.reason(),
                e.idempotencyKey(),
                "/courses/" + e.courseId(),
                "{\"courseId\":" + e.courseId() + ",\"reason\":\"" + e.reason() + "\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseCompletedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User u = userRepo.getReferenceById(e.studentId());
    var n =
        make(
                NotificationTopic.COURSE_COMPLETED,
                NotificationType.SUCCESS,
                "Hoàn thành khoá học",
                "Chúc mừng! Hoàn tất 100% khoá #" + e.courseId() + ". Hãy để lại đánh giá nhé!",
                e.idempotencyKey(),
                "/courses/" + e.courseId() + "/review",
                "{\"courseId\":" + e.courseId() + ",\"cta\":\"review\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(RefundStatusChangedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User u = userRepo.getReferenceById(e.studentId());
    // map status -> type
    NotificationType t =
        "APPROVED".equalsIgnoreCase(e.status())
            ? NotificationType.SUCCESS
            : "REJECTED".equalsIgnoreCase(e.status())
                ? NotificationType.ERROR
                : NotificationType.INFO;

    var n =
        make(
                NotificationTopic.REFUND_STATUS_CHANGED,
                t,
                "Yêu cầu hoàn tiền",
                "Trạng thái #" + e.refundId() + ": " + e.status(),
                e.idempotencyKey(),
                "/account/refunds/" + e.refundId(),
                "{\"refundId\":" + e.refundId() + ",\"status\":\"" + e.status() + "\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  // ===================== Instructor =====================

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ReviewChangedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    Long insId = courseRepo.findInstructorIdByCourseId(e.courseId());
    if (insId == null) return;
    User ins = userRepo.getReferenceById(insId);
    var n =
        make(
                NotificationTopic.NEW_REVIEW,
                NotificationType.INFO,
                "Review thay đổi",
                "Khoá #" + e.courseId() + " có review #" + e.reviewId() + " (" + e.action() + ").",
                e.idempotencyKey() + "#ins:" + insId,
                "/instructor/courses/" + e.courseId() + "/reviews/" + e.reviewId(),
                "{\"courseId\":"
                    + e.courseId()
                    + ",\"reviewId\":"
                    + e.reviewId()
                    + ",\"action\":\""
                    + e.action()
                    + "\"}")
            .toUser(ins);
    saveAndPush(n, insId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseReviewOutcomeEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User ins = userRepo.getReferenceById(e.instructorId());
    boolean approved = "APPROVED".equalsIgnoreCase(e.outcome());
    NotificationType t = approved ? NotificationType.SUCCESS : NotificationType.ERROR;
    String body = "Khoá #" + e.courseId() + " đã được " + (approved ? "DUYỆT" : "TỪ CHỐI");

    var n =
        make(
                NotificationTopic.INSTRUCTOR_COURSE_APPROVED,
                t,
                "Kết quả duyệt khoá",
                body,
                e.idempotencyKey(),
                "/instructor/courses/" + e.courseId(),
                "{\"courseId\":" + e.courseId() + ",\"outcome\":\"" + e.outcome() + "\"}")
            .toUser(ins);
    saveAndPush(n, e.instructorId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CoursePublishedToggledEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User ins = userRepo.getReferenceById(e.instructorId());
    var n =
        make(
                NotificationTopic.COURSE_PUBLISHED_TOGGLE,
                NotificationType.INFO,
                "Trạng thái hiển thị khoá",
                "Khoá #" + e.courseId() + (e.published() ? " đã PUBLISH" : " đã UNPUBLISH"),
                e.idempotencyKey(),
                "/instructor/courses/" + e.courseId(),
                "{\"courseId\":" + e.courseId() + ",\"published\":" + e.published() + "}")
            .toUser(ins);
    saveAndPush(n, e.instructorId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ContentRemovedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    Long insId = courseRepo.findInstructorIdByCourseId(e.courseId());
    if (insId == null) return;
    User ins = userRepo.getReferenceById(insId);
    var n =
        make(
                NotificationTopic.CONTENT_REMOVED,
                NotificationType.WARNING,
                "Nội dung bị gỡ",
                "Nội dung "
                    + e.contentType()
                    + " #"
                    + e.contentId()
                    + " bị gỡ. Lý do: "
                    + e.reason(),
                e.idempotencyKey(),
                "/instructor/courses/" + e.courseId(),
                "{\"courseId\":"
                    + e.courseId()
                    + ",\"type\":\""
                    + e.contentType()
                    + "\",\"contentId\":"
                    + e.contentId()
                    + "}")
            .toUser(ins);
    saveAndPush(n, insId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(OrderCreatedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User ins = userRepo.getReferenceById(e.instructorId());
    var n =
        make(
                NotificationTopic.ORDER_CREATED,
                NotificationType.INFO,
                "Đơn hàng mới",
                "Đơn #" + e.orderId() + " cho khoá #" + e.courseId(),
                e.idempotencyKey(),
                "/instructor/orders/" + e.orderId(),
                "{\"orderId\":"
                    + e.orderId()
                    + ",\"courseId\":"
                    + e.courseId()
                    + ",\"studentId\":"
                    + e.studentId()
                    + "}")
            .toUser(ins);
    saveAndPush(n, e.instructorId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(RevenueDailySummaryEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    User ins = userRepo.getReferenceById(e.instructorId());
    var n =
        make(
                NotificationTopic.REVENUE_DAILY_SUMMARY,
                NotificationType.INFO,
                "Doanh thu hằng ngày",
                "Tổng kết "
                    + e.date()
                    + ": "
                    + e.totalOrders()
                    + " đơn, "
                    + e.totalAmountMinor()
                    + " VNĐ.",
                e.idempotencyKey(),
                "/instructor/analytics?date=" + e.date(),
                "{\"date\":\""
                    + e.date()
                    + "\",\"orders\":"
                    + e.totalOrders()
                    + ",\"amount\":"
                    + e.totalAmountMinor()
                    + "}")
            .toUser(ins);
    saveAndPush(n, e.instructorId());
  }

  // ===================== Admin =====================

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(NewInstructorPendingEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_NEW_INSTRUCTOR_PENDING,
                  NotificationType.INFO,
                  "Giảng viên mới chờ duyệt",
                  "Có đăng ký mới: #" + e.instructorId(),
                  e.idempotencyKey() + "#admin:" + adminId,
                  "/admin/instructors/pending",
                  "{\"instructorId\":" + e.instructorId() + "}")
              .toUser(admin);
      saveAndPush(n, adminId);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseSubmittedForReviewEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_COURSE_SUBMITTED,
                  NotificationType.INFO,
                  "Khoá học chờ duyệt",
                  "Khoá #" + e.courseId() + " cần duyệt.",
                  e.idempotencyKey() + "#admin:" + adminId,
                  "/admin/courses/review/" + e.courseId(),
                  "{\"courseId\":" + e.courseId() + ",\"instructorId\":" + e.instructorId() + "}")
              .toUser(admin);
      saveAndPush(n, adminId);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ContentReportedEvent e) {
    if (duplicated(e.idempotencyKey())) return;
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_CONTENT_REPORTED,
                  NotificationType.WARNING,
                  "Báo cáo nội dung",
                  "Report #" + e.reportId() + " trên " + e.targetType() + " #" + e.targetId(),
                  e.idempotencyKey() + "#admin:" + adminId,
                  "/admin/reports/" + e.reportId(),
                  "{\"reportId\":"
                      + e.reportId()
                      + ",\"courseId\":"
                      + e.courseId()
                      + ",\"targetType\":\""
                      + e.targetType()
                      + "\",\"targetId\":"
                      + e.targetId()
                      + "}")
              .toUser(admin);
      saveAndPush(n, adminId);
    }
  }
}
