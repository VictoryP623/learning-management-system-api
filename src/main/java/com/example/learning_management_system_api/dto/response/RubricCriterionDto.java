package com.example.learning_management_system_api.dto.response;

import lombok.Data;

@Data
public class RubricCriterionDto {

  private Long id;
  private String name;
  private String description;
  private Integer maxScore;
  private Integer orderIndex;
}
