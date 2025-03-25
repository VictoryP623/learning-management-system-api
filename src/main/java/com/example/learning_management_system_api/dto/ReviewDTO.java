package com.example.learning_management_system_api.dto;

import com.example.learning_management_system_api.entity.Id.ReviewId;
import com.example.learning_management_system_api.entity.Review;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record ReviewDTO(
    ReviewId id,
    @NotBlank(message = "Description is required") String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public ReviewDTO(Review review) {
    this(review.getId(), review.getDescription(), review.getCreatedAt(), review.getUpdatedAt());
  }
}