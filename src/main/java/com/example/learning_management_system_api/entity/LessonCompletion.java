package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.utils.enums.LessonProgressStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lesson_completion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonCompletion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne
  @JoinColumn(name = "lesson_id")
  private Lesson lesson;

  // Trạng thái học bài
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private LessonProgressStatus status = LessonProgressStatus.NOT_STARTED;

  // Số giây đã xem (dùng cho % tiến độ)
  @Column(name = "watched_seconds")
  private Integer watchedSeconds;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  @PreUpdate
  private void ensureStatusNotNull() {
    if (this.status == null) {
      this.status = LessonProgressStatus.NOT_STARTED;
    }
  }
}
