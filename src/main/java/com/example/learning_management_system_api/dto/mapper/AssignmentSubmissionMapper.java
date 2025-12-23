package com.example.learning_management_system_api.dto.mapper;

import com.example.learning_management_system_api.dto.response.AssignmentSubmissionResponseDto;
import com.example.learning_management_system_api.entity.AssignmentSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssignmentSubmissionMapper {

  @Mapping(source = "assignment.id", target = "assignmentId")
  @Mapping(source = "student.id", target = "studentId")
  @Mapping(source = "student.user.fullname", target = "studentName")
  AssignmentSubmissionResponseDto toDto(AssignmentSubmission submission);
}
