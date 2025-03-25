package com.example.learning_management_system_api.dto.response;

import lombok.Data;

@Data
public class EarningDTO {
  private Long courseId;
  private Long instructorId;
  private Double revenue;

  public EarningDTO(Long courseId, Long instructorId, double revenue) {
    this.courseId = courseId;
    this.instructorId = instructorId;
    this.revenue = revenue;
  }
}