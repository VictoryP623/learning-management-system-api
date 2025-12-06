package com.example.learning_management_system_api.events;

/**
 * Marker interface cho tất cả các sự kiện nghiệp vụ (Domain Event). Mỗi event nên cung cấp một
 * idempotencyKey để chống bắn trùng thông báo.
 */
public interface DomainEvent {
  /** Trả về khóa idempotent duy nhất cho event này. Dùng để kiểm tra trùng khi gửi thông báo. */
  String idempotencyKey();
}
