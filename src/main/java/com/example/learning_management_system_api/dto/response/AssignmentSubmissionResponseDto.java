package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AssignmentSubmissionResponseDto {

  private Long id;
  private Long assignmentId;
  private Long studentId;
  private String studentName;

  private String textAnswer;
  private String attachmentUrl;

  private Integer score;
  private String feedback;

  private LocalDateTime submittedAt;
  private LocalDateTime gradedAt;
}
