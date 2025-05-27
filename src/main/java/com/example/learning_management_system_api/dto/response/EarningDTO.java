package com.example.learning_management_system_api.dto.response;

import lombok.Data;

@Data
public class EarningDTO {
  private Long courseId;
  private Long instructorId;
  private String courseName;
  private Integer soldCount; // Số lượng bán ra
  private Double revenue;

  public EarningDTO(
      Long courseId, Long instructorId, String courseName, Integer soldCount, Double revenue) {
    this.courseId = courseId;
    this.instructorId = instructorId;
    this.courseName = courseName;
    this.soldCount = soldCount;
    this.revenue = revenue;
  }
}