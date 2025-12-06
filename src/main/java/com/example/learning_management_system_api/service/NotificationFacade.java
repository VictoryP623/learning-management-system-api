package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.NotificationCreateRequest;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Instructor;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Gom các hàm gửi Notification theo từng tình huống nghiệp vụ. Ưu tiên "fire-and-forget": không để
 * lỗi notif chặn flow chính.
 */
@Service
@RequiredArgsConstructor
public class NotificationFacade {

  private final NotificationService notificationService;

  /* --------------- Helpers --------------- */

  private void send(
      Long recipientId,
      Long actorId,
      NotificationType type,
      String title,
      String message,
      String linkUrl) {
    try {
      NotificationCreateRequest req = new NotificationCreateRequest();
      req.setRecipientId(recipientId);
      req.setActorId(actorId);
      req.setType(type);
      req.setTitle(title);
      req.setMessage(message);
      req.setLinkUrl(linkUrl);
      notificationService.createAndDispatch(req);
    } catch (Exception ignore) {
      // Không để lỗi notif làm hỏng luồng nghiệp vụ
    }
  }

  private void sendToMany(
      List<Long> recipientIds,
      Long actorId,
      NotificationType type,
      String title,
      String message,
      String linkUrl) {
    for (Long rid : recipientIds) {
      send(rid, actorId, type, title, message, linkUrl);
    }
  }

  /* --------------- Business events --------------- */

  /** 1) Chào mừng khi đăng ký thành công */
  public void welcomeOnRegister(Long userId) {
    send(
        userId,
        null,
        NotificationType.INFO,
        "Welcome!",
        "Your account has been created successfully.",
        "/profile");
  }

  /** 2) Admin duyệt giảng viên */
  public void instructorApproved(Long instructorUserId, Long adminUserId) {
    send(
        instructorUserId,
        adminUserId,
        NotificationType.SUCCESS,
        "Instructor approved",
        "Your instructor account has been approved. Welcome aboard!",
        "/instructor-dashboard");
  }

  /** 3) Tạo bài tập mới -> gửi cho toàn bộ học viên trong khóa */
  public void newAssignmentPosted(
      Long courseId, String assignmentTitle, Long instructorUserId, List<Long> studentUserIds) {
    String title = "New assignment posted";
    String msg = "Assignment \"" + assignmentTitle + "\" has been posted.";
    String link = "/courses/" + courseId + "/assignments";
    sendToMany(studentUserIds, instructorUserId, NotificationType.INFO, title, msg, link);
  }

  /** 4) Chấm điểm bài tập -> gửi cho từng sinh viên */
  public void gradeReleased(
      Long studentUserId,
      Long instructorUserId,
      Long courseId,
      String assignmentTitle,
      String scoreStr) {
    String title = "Grade released";
    String msg = "You got " + scoreStr + " for \"" + assignmentTitle + "\".";
    String link = "/courses/" + courseId + "/grades";
    send(studentUserId, instructorUserId, NotificationType.SUCCESS, title, msg, link);
  }

  /** 5) Mua khóa học thành công -> thông báo cho học viên */
  public void purchaseSucceededStudent(
      Long studentUserId, String orderCode, List<String> courseTitles) {
    String title = "Purchase successful";
    String msg = "Order " + orderCode + " completed: " + String.join(", ", courseTitles);
    String link = "/my-courses";
    send(studentUserId, null, NotificationType.SUCCESS, title, msg, link);
  }

  /** 6) Mua khóa học -> thông báo cho giảng viên của từng course */
  public void purchaseNotifyInstructor(Course course, Long studentUserId, String studentName) {
    Instructor ins = course.getInstructor();
    if (ins != null && ins.getUser() != null) {
      Long instructorUserId = ins.getUser().getId();
      String title = "New enrollment";
      String msg =
          "A student (" + studentName + ") purchased your course \"" + course.getName() + "\".";
      String link = "/instructor-dashboard";
      send(instructorUserId, studentUserId, NotificationType.INFO, title, msg, link);
    }
  }
}
