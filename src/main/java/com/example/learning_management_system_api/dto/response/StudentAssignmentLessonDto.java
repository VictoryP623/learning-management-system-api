package com.example.learning_management_system_api.dto.response;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAssignmentLessonDto {
  private Long lessonId;
  private String lessonName;
  private List<StudentAssignmentItemDto> assignments;
}
