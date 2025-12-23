package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Rubric;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubricRepository extends JpaRepository<Rubric, Long> {

  Optional<Rubric> findByAssignment_Id(Long assignmentId);
}
