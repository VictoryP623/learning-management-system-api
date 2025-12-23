package com.example.learning_management_system_api.entity.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class FollowId implements Serializable {
  @Column(name = "student_id")
  private Long studentId;

  @Column(name = "instructor_id")
  private Long instructorId;
}
