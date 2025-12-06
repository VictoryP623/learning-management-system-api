package com.example.learning_management_system_api.events;

public final class AdminEvents {

  private AdminEvents() {}

  /** Có đăng ký mới với vai trò Instructor chờ duyệt. */
  public record NewInstructorPendingEvent(Long instructorId) implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "AdminNewInstructorPending:" + instructorId;
    }
  }

  /** Khoá học được submit để duyệt. */
  public record CourseSubmittedForReviewEvent(Long courseId, Long instructorId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "AdminCourseSubmitted:" + courseId;
    }
  }

  /** Có report nội dung cần xem xét. */
  public record ContentReportedEvent(Long reportId, Long courseId, String targetType, Long targetId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "AdminContentReported:" + reportId;
    }
  }
}
