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

  /** Có user đăng ký mới (student/instructor). */
  public record UserRegisteredEvent(Long userId, String email, String role) implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "AdminUserRegistered:" + userId;
    }
  }

  /** Instructor tạo course mới (thường status PENDING). */
  public record InstructorCreatedCourseEvent(Long courseId, Long instructorId, String courseName)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "AdminInstructorCreatedCourse:" + courseId;
    }
  }
}
