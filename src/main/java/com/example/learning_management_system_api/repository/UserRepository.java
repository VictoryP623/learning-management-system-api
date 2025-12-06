package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.utils.enums.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Page<User> findByRole(UserRole role, Pageable pageable);

  List<User> findByRole(UserRole role);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<User> findByVerificationCode(String verificationCode);

  Optional<User> findByRefreshToken(String refreshToken);

  // === NEW: Lấy danh sách userId theo role (để notify Admins) ===
  @Query("select u.id from User u where u.role = :role")
  List<Long> findIdsByRole(@Param("role") UserRole role);

  // === NEW: Helper tiện dụng ===
  default List<Long> findAdminIds() {
    return findIdsByRole(UserRole.Admin);
  }
}
