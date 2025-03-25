package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.QuizAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
  List<QuizAttempt> findByUserId(Long userId);
}
