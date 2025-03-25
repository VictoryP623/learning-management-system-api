package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Id.ReviewId;
import com.example.learning_management_system_api.entity.Review;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, ReviewId> {
  Page<Review> findByCourseId(Long courseId, Pageable pageable);

  List<Review> findByCourseId(Long courseId);
}
