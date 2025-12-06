package com.example.learning_management_system_api.events;

public final class InstructorEvents {

  private InstructorEvents() {}

  /** Có review mới/sửa/xoá trên khoá học của instructor. */
  public record ReviewChangedEvent(Long courseId, Long reviewId, String action, Long actorId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "Review:" + reviewId + ":" + action;
    }
  }

  /** Kết quả duyệt khoá từ Admin: APPROVED / REJECTED. */
  public record CourseReviewOutcomeEvent(Long courseId, Long instructorId, String outcome)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "CourseReviewOutcome:" + courseId + ":" + outcome;
    }
  }

  /** Bật/tắt publish khoá học. */
  public record CoursePublishedToggledEvent(Long courseId, Long instructorId, boolean published)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "CoursePublishedToggle:" + courseId + ":" + published;
    }
  }

  /** Nội dung bị gỡ bởi moderation (lesson/quiz/material...). */
  public record ContentRemovedEvent(
      Long courseId, String contentType, Long contentId, String reason, Long moderatorId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "ContentRemoved:" + contentType + ":" + contentId;
    }
  }

  /** Đơn hàng/enrollment mới liên quan tới khoá của instructor. */
  public record OrderCreatedEvent(Long orderId, Long courseId, Long instructorId, Long studentId)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "OrderCreated:" + orderId;
    }
  }

  /** Tổng kết doanh thu hằng ngày (cron/digest). */
  public record RevenueDailySummaryEvent(
      Long instructorId, String date /*yyyy-MM-dd*/, Long totalOrders, Long totalAmountMinor)
      implements DomainEvent {
    @Override
    public String idempotencyKey() {
      return "RevenueDaily:" + instructorId + ":" + date;
    }
  }
}
