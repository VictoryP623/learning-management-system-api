package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.repository.EnrollRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollService {

  private final EnrollRepository enrollRepository;

  /** Kiểm tra một student (theo student.id) đã enroll vào course (theo course.id) hay chưa. */
  public boolean isStudentEnrolledInCourse(Long studentId, Long courseId) {
    return enrollRepository.existsByStudent_IdAndCourse_Id(studentId, courseId);
  }
}
