package com.example.learning_management_system_api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
  private Long id;

  @Min(value = 1000, message = "Price must be at least 1000")
  @NotNull
  private Double price;

  @NotBlank(message = "Thumbnail is required")
  private String thumbnail;

  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
  private String name;

  @NotNull(message = "Category ID is required")
  private Long categoryId;
}
