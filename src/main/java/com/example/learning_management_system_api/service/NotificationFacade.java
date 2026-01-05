package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.NotificationCreateRequest;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Instructor;
import com.example.learning_management_system_api.utils.enums.NotificationTopic;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Gom các hàm gửi Notification theo từng tình huống nghiệp vụ. Ưu tiên fire-and-forget: không để
 * lỗi notif chặn flow chính.
 */
@Service
@RequiredArgsConstructor
public class NotificationFacade {

  private final NotificationService notificationService;

  /* --------------- Helpers --------------- */

  private static String safe(String s) {
    return (s == null) ? "" : s;
  }

  private static String idem(String prefix, String... parts) {
    // idempotency_key max length 191 -> giữ ngắn, ổn định.
    StringBuilder sb = new StringBuilder(prefix);
    for (String p : parts) {
      if (p == null || p.isBlank()) continue;
      sb.append(":").append(p);
    }
    String out = sb.toString();
    return out.length() <= 191 ? out : out.substring(0, 191);
  }

  private void send(
      Long recipientId,
      Long actorId,
      NotificationType type,
      NotificationTopic topic,
      String title,
      String message,
      String linkUrl,
      String idempotencyKey) {

    try {
      NotificationCreateRequest req = new NotificationCreateRequest();
      req.setRecipientId(recipientId);
      req.setActorId(actorId);
      req.setType(type);
      req.setTopic(topic);
      req.setTitle(title);
      req.setMessage(message);
      req.setLinkUrl(linkUrl);

      req.setIdempotencyKey(
          (idempotencyKey != null && !idempotencyKey.isBlank())
              ? idempotencyKey
              : "auto:" + UUID.randomUUID());

      notificationService.createAndDispatch(req);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendToMany(
      List<Long> recipientIds,
      Long actorId,
      NotificationType type,
      NotificationTopic topic,
      String title,
      String message,
      String linkUrl,
      String idempotencyKeyPrefix) {

    if (recipientIds == null || recipientIds.isEmpty()) return;

    for (Long rid : recipientIds) {
      String idemKey = idem(idempotencyKeyPrefix, String.valueOf(rid));
      send(rid, actorId, type, topic, title, message, linkUrl, idemKey);
    }
  }

  /* --------------- Business events --------------- */

  public void welcomeOnRegister(Long userId) {
    send(
        userId,
        null,
        NotificationType.INFO,
        NotificationTopic.STUDY_REMINDER,
        "Welcome!",
        "Your account has been created successfully.",
        "/profile",
        idem("welcome", String.valueOf(userId)));
  }

  public void instructorApproved(Long instructorUserId, Long adminUserId) {
    send(
        instructorUserId,
        adminUserId,
        NotificationType.SUCCESS,
        NotificationTopic.INSTRUCTOR_COURSE_APPROVED,
        "Instructor approved",
        "Your instructor account has been approved. Welcome aboard!",
        "/instructor-dashboard",
        idem("instructorApproved", String.valueOf(instructorUserId)));
  }

  public void newAssignmentPosted(
      Long courseId, String assignmentTitle, Long instructorUserId, List<Long> studentUserIds) {

    String title = "New assignment posted";
    String msg = "Assignment \"" + safe(assignmentTitle) + "\" has been posted.";
    String link = "/courses/" + courseId + "/assignments";

    sendToMany(
        studentUserIds,
        instructorUserId,
        NotificationType.INFO,
        NotificationTopic.ASSIGNMENT_SUBMITTED,
        title,
        msg,
        link,
        idem("newAssignment", String.valueOf(courseId), safe(assignmentTitle)));
  }

  public void gradeReleased(
      Long studentUserId,
      Long instructorUserId,
      Long courseId,
      String assignmentTitle,
      String scoreStr) {

    String title = "Grade released";
    String msg = "You got " + safe(scoreStr) + " for \"" + safe(assignmentTitle) + "\".";
    String link = "/courses/" + courseId + "/grades";

    send(
        studentUserId,
        instructorUserId,
        NotificationType.SUCCESS,
        NotificationTopic.ASSIGNMENT_GRADED,
        title,
        msg,
        link,
        idem(
            "gradeReleased",
            String.valueOf(courseId),
            String.valueOf(studentUserId),
            safe(assignmentTitle)));
  }

  /** 5) Mua khóa học thành công -> thông báo cho học viên */
  public void purchaseSucceededStudent(
      Long studentUserId, String orderCode, List<String> courseTitles) {

    String title = "Purchase successful";
    String msg =
        "Order "
            + safe(orderCode)
            + " completed: "
            + String.join(", ", Objects.requireNonNullElse(courseTitles, List.of()));
    String link = "/my-courses";

    send(
        studentUserId,
        null,
        NotificationType.SUCCESS,
        NotificationTopic.ORDER_CREATED,
        title,
        msg,
        link,
        idem("purchaseStudent", safe(orderCode), String.valueOf(studentUserId)));
  }

  /** 6a) Mua khóa học -> thông báo cho giảng viên (cách cũ, nhận Course entity) */
  public void purchaseNotifyInstructor(Course course, Long studentUserId, String studentName) {
    if (course == null) return;
    Instructor ins = course.getInstructor();

    if (ins != null && ins.getUser() != null) {
      Long instructorUserId = ins.getUser().getId();

      String title = "New enrollment";
      String msg =
          "A student ("
              + safe(studentName)
              + ") purchased your course \""
              + safe(course.getName())
              + "\".";
      String link = "/instructor-dashboard";

      send(
          instructorUserId,
          studentUserId,
          NotificationType.INFO,
          NotificationTopic.STUDENT_ENROLLED,
          title,
          msg,
          link,
          idem(
              "purchaseInstructor",
              String.valueOf(course.getId()),
              String.valueOf(studentUserId),
              String.valueOf(instructorUserId)));
    }
  }

  /**
   * 6b) Mua khóa học -> thông báo cho giảng viên (an toàn cho AFTER_COMMIT): không truyền entity
   * LAZY, chỉ truyền IDs.
   */
  public void purchaseNotifyInstructorByIds(
      Long courseId, Long instructorUserId, Long studentUserId, String studentName) {

    String title = "New enrollment";
    String msg = "A student (" + safe(studentName) + ") purchased your course.";
    String link = "/instructor-dashboard";

    send(
        instructorUserId,
        studentUserId,
        NotificationType.INFO,
        NotificationTopic.STUDENT_ENROLLED,
        title,
        msg,
        link,
        idem(
            "purchaseInstructor",
            String.valueOf(courseId),
            String.valueOf(studentUserId),
            String.valueOf(instructorUserId)));
  }
}
