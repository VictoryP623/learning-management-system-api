package com.example.learning_management_system_api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class RubricResponseDto {

  private Long id;
  private Long assignmentId;

  private String title;
  private String description;

  private List<RubricCriterionDto> criteria;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
