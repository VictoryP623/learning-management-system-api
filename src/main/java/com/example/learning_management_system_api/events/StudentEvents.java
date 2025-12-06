package com.example.learning_management_system_api.events;

public final class StudentEvents {

  private StudentEvents() {}

  /** Khi trạng thái khoá học đổi (pending/rejected -> active ...) */
  public record CourseStatusChangedEvent(Long courseId, String fromStatus, String toStatus)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "CourseStatusChanged:" + courseId + ":" + fromStatus + "->" + toStatus;
    }
  }

  /** Khi thêm bài học mới vào khoá. */
  public record LessonCreatedEvent(Long courseId, Long lessonId, Long instructorId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "LessonCreated:" + lessonId;
    }
  }

  /** Khi cập nhật bài học (đổi video/tài liệu...). */
  public record LessonUpdatedEvent(Long courseId, Long lessonId) implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "LessonUpdated:" + lessonId;
    }
  }

  /** Khi publish quiz trong khoá. */
  public record QuizPublishedEvent(Long courseId, Long quizId) implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "QuizPublished:" + quizId;
    }
  }

  /** Nhắc học (cron): 7 ngày không học, mua nhưng chưa bắt đầu... */
  public record StudyReminderEvent(Long studentId, Long courseId, String reason)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "StudyReminder:" + studentId + ":" + courseId + ":" + reason;
    }
  }

  /** Khi học viên hoàn thành 100% tiến độ khoá. */
  public record CourseCompletedEvent(Long studentId, Long courseId) implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "CourseCompleted:" + studentId + ":" + courseId;
    }
  }

  /** Khi yêu cầu hoàn tiền đổi trạng thái. */
  public record RefundStatusChangedEvent(Long refundId, Long studentId, String status)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "Refund:" + refundId + ":" + status;
    }
  }
}
