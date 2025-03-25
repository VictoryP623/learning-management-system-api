package com.example.learning_management_system_api.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class AnswerOption {
  private Integer keyValue;
  @NotBlank private String text;
  @NotNull private Boolean isCorrect;
}
