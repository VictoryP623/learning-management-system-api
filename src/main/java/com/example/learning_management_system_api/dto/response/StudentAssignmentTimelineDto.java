package com.example.learning_management_system_api.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAssignmentTimelineDto {
  private Long courseId;
  private List<StudentAssignmentLessonDto> lessons;
}
