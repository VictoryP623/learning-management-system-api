package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

  Page<Quiz> findByLessonId(Long lessonId, Pageable pageable);
}
