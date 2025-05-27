package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Cart;
import com.example.learning_management_system_api.entity.Course;
import com.example.learning_management_system_api.entity.Id.CartId;
import com.example.learning_management_system_api.entity.Student;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, CartId> {
  List<Cart> findByStudentId(Long studentId);

  List<Cart> findByStudent(Student student);

  Page<Cart> findByStudentId(Long studentId, Pageable pageable);

  void deleteAllByStudent(Student student);

  void deleteByStudentAndCourse(Student student, Course course);
}
