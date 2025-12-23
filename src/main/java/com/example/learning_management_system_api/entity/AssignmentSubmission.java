package com.example.learning_management_system_api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "assignment_submission",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_assignment_student",
          columnNames = {"assignment_id", "student_id"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignment_id", nullable = false)
  private Assignment assignment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Column(name = "submitted_at")
  private LocalDateTime submittedAt;

  @Column(name = "text_answer", columnDefinition = "TEXT")
  private String textAnswer;

  // Nếu sau dùng upload file -> lưu URL ở đây
  @Column(name = "attachment_url")
  private String attachmentUrl;

  // Điểm chấm
  @Column(name = "score")
  private Integer score;

  // Feedback của giảng viên
  @Column(name = "feedback", columnDefinition = "TEXT")
  private String feedback;

  @Column(name = "graded_at")
  private LocalDateTime gradedAt;
}
