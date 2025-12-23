package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

  Optional<Student> findByUserId(Long userId);

  Optional<Student> findById(Long id);

  boolean existsById(Long id);

}
