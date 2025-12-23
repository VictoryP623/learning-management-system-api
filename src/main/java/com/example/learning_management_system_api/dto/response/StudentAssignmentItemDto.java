package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAssignmentItemDto {
  private Long assignmentId;
  private String title;

  private LocalDateTime dueAt; // đổi từ deadline -> dueAt cho khớp entity
  private Integer maxScore;

  // NOT_SUBMITTED | SUBMITTED | GRADED
  private String status;

  private LocalDateTime submittedAt;
  private Integer score;
  private String feedback;

  private boolean late;
}
