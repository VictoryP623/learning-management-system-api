package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.utils.enums.NotificationType;   // enum mức độ: INFO/SUCCESS/...
import com.example.learning_management_system_api.utils.enums.NotificationTopic;  // enum chủ đề nghiệp vụ
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "notification",
    indexes = {
      @Index(
          name = "idx_notification_recipient_read_created",
          columnList = "recipient_id, read_flag, created_at"),
      @Index(name = "idx_notification_created", columnList = "created_at"),
      @Index(name = "ux_notification_idem", columnList = "idempotency_key", unique = true)
    })
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Người nhận thông báo (bắt buộc) */
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "recipient_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_notification_recipient"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User recipient;

  /** (Tuỳ chọn) Người tạo tác động dẫn đến thông báo, ví dụ giảng viên/chấm điểm */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id", foreignKey = @ForeignKey(name = "fk_notification_actor"))
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User actor;

  /** Phân loại để FE hiển thị icon/màu (mức độ) */
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private NotificationType type;

  /** Chủ đề nghiệp vụ để FE định tuyến/CTA (course/lesson/quiz/...) */
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "topic", nullable = false, length = 64)
  private NotificationTopic topic;

  /** Khoá chống trùng (idempotent) để tránh bắn lặp khi retry/rollback */
  @NotBlank
  @Column(name = "idempotency_key", nullable = false, length = 191)
  private String idempotencyKey;

  /** Tiêu đề ngắn gọn */
  @NotBlank
  @Column(name = "title", nullable = false, length = 255)
  private String title;

  /** Nội dung chi tiết */
  @NotBlank
  @Column(name = "message", nullable = false, length = 2000)
  private String message;

  /** Link (deep-link) để điều hướng tới màn hình liên quan (vd: /courses/12/assignments/3) */
  @Column(name = "link_url", length = 1000)
  private String linkUrl;

  /** Payload JSON bổ sung (tuỳ nghi cho FE) */
  @Lob
  @Column(name = "data_json")
  private String dataJson;

  /** Đã đọc hay chưa */
  @Column(name = "read_flag", nullable = false)
  @Builder.Default
  private boolean readFlag = false;

  /** Thời điểm tạo (tự sinh) */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /* ===== Helpers tiện dùng trong Notifier ===== */
  public Notification toUser(User u) {
    this.setRecipient(u);
    return this;
  }
}
