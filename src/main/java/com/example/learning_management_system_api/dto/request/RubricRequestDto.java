package com.example.learning_management_system_api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RubricRequestDto(
    @NotBlank String title,
    String description,
    @NotEmpty List<@Valid RubricCriterionInput> criteria) {

  public record RubricCriterionInput(
      @NotBlank String name,
      String description,
      @NotNull @Min(0) Integer maxScore,
      Integer orderIndex) {}
}
