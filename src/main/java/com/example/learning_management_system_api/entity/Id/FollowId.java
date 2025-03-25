package com.example.learning_management_system_api.entity.Id;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowId implements Serializable {
  private Long studentId;
  private Long instructorId;
}
