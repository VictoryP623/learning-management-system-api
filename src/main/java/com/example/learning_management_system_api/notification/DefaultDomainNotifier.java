package com.example.learning_management_system_api.notification;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import com.example.learning_management_system_api.dto.mapper.NotificationMapper;
import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.entity.Notification;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.events.AdminEvents.*;
import com.example.learning_management_system_api.events.InstructorEvents.*;
import com.example.learning_management_system_api.events.StudentEvents.*;
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

  private boolean duplicatedExact(String idempotencyKey) {
    return notificationRepo.existsByIdempotencyKey(idempotencyKey);
  }

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

  private void saveAndPush(Notification n, Long userId) {
    try {
      Notification saved = notificationService.save(n);
      NotificationResponse dto = notificationMapper.toDto(saved);
      messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications", dto);
    } catch (DataIntegrityViolationException ignore) {
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseStatusChangedEvent e) {
    String link = "/course/" + e.courseId();
    String data =
        "{\"courseId\":"
            + e.courseId()
            + ",\"from\":\""
            + e.fromStatus()
            + "\",\"to\":\""
            + e.toStatus()
            + "\"}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#enroll:" + sid;
      if (duplicatedExact(idem)) continue;

      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.COURSE_STATUS_CHANGED,
                  NotificationType.INFO,
                  "Khoá học cập nhật trạng thái",
                  "Khoá #" + e.courseId() + " chuyển " + e.fromStatus() + " → " + e.toStatus(),
                  idem,
                  link,
                  data)
              .toUser(u);
      saveAndPush(n, sid);
    }

    for (Long uid : cartRepo.findUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#watch:" + uid;
      if (duplicatedExact(idem)) continue;

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
                  idem,
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
    String link = "/courses/" + e.courseId() + "/learn";

    String data =
        "{"
            + "\"courseId\":"
            + e.courseId()
            + ",\"lessonId\":"
            + e.lessonId()
            + ",\"instructorUserId\":"
            + (e.instructorUserId() == null ? "null" : e.instructorUserId())
            + "}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#student:" + sid;
      if (duplicatedExact(idem)) continue;

      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.LESSON_CREATED,
                  NotificationType.INFO,
                  "Bài học mới",
                  "Khoá #" + e.courseId() + " vừa thêm bài #" + e.lessonId(),
                  idem,
                  link,
                  data)
              .toUser(u);

      if (e.instructorUserId() != null) {
        n.setActor(userRepo.getReferenceById(e.instructorUserId()));
      }

      saveAndPush(n, sid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(LessonUpdatedEvent e) {
    String link = "/courses/" + e.courseId() + "/learn";
    String data = "{\"courseId\":" + e.courseId() + ",\"lessonId\":" + e.lessonId() + "}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#student:" + sid;
      if (duplicatedExact(idem)) continue;

      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.LESSON_UPDATED,
                  NotificationType.INFO,
                  "Bài học cập nhật",
                  "Bài #" + e.lessonId() + " của khoá #" + e.courseId() + " đã cập nhật.",
                  idem,
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
    String link = "/courses/" + e.courseId() + "/learn";
    String data = "{\"courseId\":" + e.courseId() + ",\"quizId\":" + e.quizId() + "}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#student:" + sid;
      if (duplicatedExact(idem)) continue;

      User u = userRepo.getReferenceById(sid);
      var n =
          make(
                  NotificationTopic.QUIZ_PUBLISHED,
                  NotificationType.INFO,
                  "Quiz mới mở",
                  "Khoá #" + e.courseId() + " có quiz #" + e.quizId() + " vừa mở.",
                  idem,
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
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User u = userRepo.getReferenceById(e.studentId());
    var n =
        make(
                NotificationTopic.STUDY_REMINDER,
                NotificationType.INFO,
                "Nhắc học",
                "Đã " + e.reason(),
                idem,
                "/courses/" + e.courseId() + "/learn",
                "{\"courseId\":" + e.courseId() + ",\"reason\":\"" + e.reason() + "\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseCompletedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User u = userRepo.getReferenceById(e.studentId());
    var n =
        make(
                NotificationTopic.COURSE_COMPLETED,
                NotificationType.SUCCESS,
                "Hoàn thành khoá học",
                "Chúc mừng! Hoàn tất 100% khoá #" + e.courseId() + ". Hãy để lại đánh giá nhé!",
                idem,
                "/courses/" + e.courseId() + "/review",
                "{\"courseId\":" + e.courseId() + ",\"cta\":\"review\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(RefundStatusChangedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User u = userRepo.getReferenceById(e.studentId());
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
                idem,
                "/account/refunds/" + e.refundId(),
                "{\"refundId\":" + e.refundId() + ",\"status\":\"" + e.status() + "\"}")
            .toUser(u);
    saveAndPush(n, e.studentId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(AssignmentGradedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    Long sid = e.studentUserId();
    User stu = userRepo.getReferenceById(sid);

    String link = "/courses/" + e.courseId() + "/assignments";

    String data =
        "{"
            + "\"courseId\":"
            + e.courseId()
            + ",\"lessonId\":"
            + e.lessonId()
            + ",\"assignmentId\":"
            + e.assignmentId()
            + ",\"score\":"
            + e.score()
            + ",\"maxScore\":"
            + e.maxScore()
            + "}";

    String body =
        "Bài \""
            + e.assignmentTitle()
            + "\" đã được chấm: "
            + e.score()
            + "/"
            + e.maxScore()
            + (e.feedback() != null && !e.feedback().isBlank()
                ? ". Feedback: " + e.feedback()
                : "");

    var n =
        make(
                NotificationTopic.ASSIGNMENT_GRADED,
                NotificationType.SUCCESS,
                "Bài tập đã được chấm",
                body,
                idem,
                link,
                data)
            .toUser(stu);

    if (e.instructorUserId() != null) {
      n.setActor(userRepo.getReferenceById(e.instructorUserId()));
    }

    saveAndPush(n, sid);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(AssignmentCreatedEvent e) {
    String link = "/courses/" + e.courseId() + "/assignments";

    String data =
        "{"
            + "\"courseId\":"
            + e.courseId()
            + ",\"lessonId\":"
            + e.lessonId()
            + ",\"assignmentId\":"
            + e.assignmentId()
            + ",\"assignmentTitle\":\""
            + (e.assignmentTitle() == null ? "" : e.assignmentTitle().replace("\"", "\\\""))
            + "\""
            + "}";

    for (Long sid : enrollRepo.findStudentUserIdsByCourseId(e.courseId())) {
      String idem = e.idempotencyKey() + "#student:" + sid;
      if (duplicatedExact(idem)) continue;

      User u = userRepo.getReferenceById(sid);

      var n =
          make(
                  NotificationTopic.ASSIGNMENT_CREATED,
                  NotificationType.INFO,
                  "Bài tập mới",
                  "Khoá #"
                      + e.courseId()
                      + " vừa có bài tập mới: "
                      + (e.assignmentTitle() == null ? "" : e.assignmentTitle()),
                  idem,
                  link,
                  data)
              .toUser(u);

      if (e.instructorUserId() != null) {
        n.setActor(userRepo.getReferenceById(e.instructorUserId()));
      }

      saveAndPush(n, sid);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ReviewChangedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    Long instructorUserId = courseRepo.findInstructorUserIdByCourseId(e.courseId());
    if (instructorUserId == null) return;

    User ins = userRepo.getReferenceById(instructorUserId);

    String link = "/course/" + e.courseId();

    String data =
        "{"
            + "\"courseId\":"
            + e.courseId()
            + ",\"studentUserId\":"
            + e.studentUserId()
            + ",\"action\":\""
            + e.action()
            + "\""
            + (e.rating() != null ? ",\"rating\":" + e.rating() : "")
            + "}";

    String actionText =
        "CREATED".equalsIgnoreCase(e.action())
            ? "tạo"
            : "UPDATED".equalsIgnoreCase(e.action()) ? "cập nhật" : "xóa";

    String body =
        "Học viên đã "
            + actionText
            + " đánh giá cho khoá #"
            + e.courseId()
            + (e.rating() != null ? " (rating: " + e.rating() + ")" : "");

    var n =
        make(
                NotificationTopic.NEW_REVIEW,
                NotificationType.INFO,
                "Review thay đổi",
                body,
                idem + "#ins:" + instructorUserId,
                link,
                data)
            .toUser(ins);

    if (e.studentUserId() != null) {
      n.setActor(userRepo.getReferenceById(e.studentUserId()));
    }

    saveAndPush(n, instructorUserId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseReviewOutcomeEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User ins = userRepo.getReferenceById(e.instructorUserId());
    boolean approved = "APPROVED".equalsIgnoreCase(e.outcome());
    NotificationType t = approved ? NotificationType.SUCCESS : NotificationType.ERROR;

    NotificationTopic topic =
        approved
            ? NotificationTopic.INSTRUCTOR_COURSE_APPROVED
            : NotificationTopic.INSTRUCTOR_COURSE_REJECTED;

    String body = "Khoá #" + e.courseId() + " đã được " + (approved ? "DUYỆT" : "TỪ CHỐI");

    var n =
        make(
                topic,
                t,
                "Kết quả duyệt khoá",
                body,
                idem,
                "/instructor/course/" + e.courseId(),
                "{\"courseId\":" + e.courseId() + ",\"outcome\":\"" + e.outcome() + "\"}")
            .toUser(ins);

    saveAndPush(n, e.instructorUserId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CoursePublishedToggledEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User ins = userRepo.getReferenceById(e.instructorId());
    var n =
        make(
                NotificationTopic.COURSE_PUBLISHED_TOGGLE,
                NotificationType.INFO,
                "Trạng thái hiển thị khoá",
                "Khoá #" + e.courseId() + (e.published() ? " đã PUBLISH" : " đã UNPUBLISH"),
                idem,
                "/instructor/course/" + e.courseId(),
                "{\"courseId\":" + e.courseId() + ",\"published\":" + e.published() + "}")
            .toUser(ins);
    saveAndPush(n, e.instructorId());
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ContentRemovedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

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
                idem,
                "/instructor/course/" + e.courseId(),
                "{\"courseId\":"
                    + e.courseId()
                    + ",\"type\":\""
                    + e.contentType()
                    + "\",\"contentId\":"
                    + e.contentId()
                    + "}")
            .toUser(ins);

    if (e.moderatorId() != null) {
      n.setActor(userRepo.getReferenceById(e.moderatorId()));
    }

    saveAndPush(n, insId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(OrderCreatedEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    User ins = userRepo.getReferenceById(e.instructorId());
    var n =
        make(
                NotificationTopic.ORDER_CREATED,
                NotificationType.INFO,
                "Đơn hàng mới",
                "Đơn #" + e.orderId() + " cho khoá #" + e.courseId(),
                idem,
                "/instructor-dashboard",
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
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

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
                idem,
                "/instructor/statistics",
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

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(StudentEnrolledCourseEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    Long insId = e.instructorUserId();
    User ins = userRepo.getReferenceById(insId);

    String link = "/instructor/course/" + e.courseId();
    String data =
        "{" + "\"courseId\":" + e.courseId() + ",\"studentId\":" + e.studentUserId() + "}";

    var n =
        make(
                NotificationTopic.STUDENT_ENROLLED,
                NotificationType.INFO,
                "Có học viên đăng ký",
                e.studentName() + " đã đăng ký khoá \"" + e.courseName() + "\".",
                idem,
                link,
                data)
            .toUser(ins);

    n.setActor(userRepo.getReferenceById(e.studentUserId()));
    saveAndPush(n, insId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(StudentSubmittedAssignmentEvent e) {
    String idem = e.idempotencyKey();
    if (duplicatedExact(idem)) return;

    Long insId = e.instructorUserId();
    User ins = userRepo.getReferenceById(insId);

    String link = "/instructor/lessons/" + e.lessonId() + "/assignments";

    String data =
        "{"
            + "\"courseId\":"
            + e.courseId()
            + ",\"lessonId\":"
            + e.lessonId()
            + ",\"assignmentId\":"
            + e.assignmentId()
            + ",\"studentId\":"
            + e.studentUserId()
            + "}";

    var n =
        make(
                NotificationTopic.ASSIGNMENT_SUBMITTED,
                NotificationType.INFO,
                "Bài nộp mới",
                e.studentName() + " vừa nộp bài \"" + e.assignmentTitle() + "\".",
                idem,
                link,
                data)
            .toUser(ins);

    n.setActor(userRepo.getReferenceById(e.studentUserId()));
    saveAndPush(n, insId);
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(NewInstructorPendingEvent e) {
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      String idem = e.idempotencyKey() + "#admin:" + adminId;
      if (duplicatedExact(idem)) continue;

      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_NEW_INSTRUCTOR_PENDING,
                  NotificationType.INFO,
                  "Giảng viên mới chờ duyệt",
                  "Có đăng ký mới: #" + e.instructorId(),
                  idem,
                  "/admin-dashboard",
                  "{\"instructorId\":" + e.instructorId() + "}")
              .toUser(admin);
      saveAndPush(n, adminId);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(CourseSubmittedForReviewEvent e) {
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      String idem = e.idempotencyKey() + "#admin:" + adminId;
      if (duplicatedExact(idem)) continue;

      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_COURSE_SUBMITTED,
                  NotificationType.INFO,
                  "Khoá học chờ duyệt",
                  "Khoá #" + e.courseId() + " cần duyệt.",
                  idem,
                  "/admin-dashboard",
                  "{\"courseId\":" + e.courseId() + ",\"instructorId\":" + e.instructorId() + "}")
              .toUser(admin);
      saveAndPush(n, adminId);
    }
  }

  @Async
  @TransactionalEventListener(phase = AFTER_COMMIT)
  @Override
  public void handle(ContentReportedEvent e) {
    List<Long> adminIds = userRepo.findAdminIds();
    for (Long adminId : adminIds) {
      String idem = e.idempotencyKey() + "#admin:" + adminId;
      if (duplicatedExact(idem)) continue;

      User admin = userRepo.getReferenceById(adminId);
      var n =
          make(
                  NotificationTopic.ADMIN_CONTENT_REPORTED,
                  NotificationType.WARNING,
                  "Báo cáo nội dung",
                  "Report #" + e.reportId() + " trên " + e.targetType() + " #" + e.targetId(),
                  idem,
                  "/admin-dashboard",
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
