package com.example.learning_management_system_api.dto;

import com.example.learning_management_system_api.entity.Id.ReportId;
import com.example.learning_management_system_api.entity.Report;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ReportDTO(
    ReportId id,
    @NotBlank(message = "Description is required") String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public ReportDTO(Report report) {
    this(report.getId(), report.getDescription(), report.getCreatedAt(), report.getUpdatedAt());
  }
}