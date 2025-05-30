package com.example.learning_management_system_api.entity.Id;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartId implements Serializable {
  private Long studentId;
  private Long courseId;
}