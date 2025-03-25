package com.example.learning_management_system_api.dto.response;

import com.example.learning_management_system_api.entity.Instructor;

public record InstructorResponseDTO(String fullname, String email) {
  public InstructorResponseDTO(Instructor instructor) {
    this(instructor.getUser().getFullname(), instructor.getUser().getEmail());
  }
}
