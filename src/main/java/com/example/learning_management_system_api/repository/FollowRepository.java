package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Follow;
import com.example.learning_management_system_api.entity.Id.FollowId;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
  Optional<Page<Follow>> findByStudentId(Long studentId, Pageable pageable);

  boolean existsByStudentIdAndInstructorId(Long studentId, Long instructorId);

  Page<Follow> findAllByStudentId(Long studentId, Pageable pageable);

  Page<Follow> findAllByInstructorId(Long instructorId, Pageable pageable);
}
