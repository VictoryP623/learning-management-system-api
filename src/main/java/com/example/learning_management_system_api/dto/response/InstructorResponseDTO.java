package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.entity.Instructor;

public record InstructorResponseDTO(Long id, String fullname, String email) {
  public InstructorResponseDTO(Instructor instructor) {
    this(instructor.getId(), instructor.getUser().getFullname(), instructor.getUser().getEmail());
  }
}
