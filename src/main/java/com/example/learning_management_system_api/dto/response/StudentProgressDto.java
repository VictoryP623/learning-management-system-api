package com.example.learning_management_system_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Thông tin tiến độ học tập của 1 học viên trong 1 khoá học. */ 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProgressDto {

  private Long studentId;
  private String fullname;
  private String email;

  private long lessonsCompleted;
  private long totalLessons;

  private long assignmentsSubmitted;
  private long totalAssignments;
  private Double avgAssignmentScore; // có thể null nếu chưa có điểm

  private long quizzesAttempted;
  private long totalQuizzes;

  private double progressPercent; // 0–100
}
