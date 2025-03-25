package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Purchase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
  List<Purchase> findDistinctCoursesByStudent_User_IdAndIsPaidTrue(Long userId);
}
