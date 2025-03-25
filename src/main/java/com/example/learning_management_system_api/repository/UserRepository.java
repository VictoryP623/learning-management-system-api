package com.example.learning_management_system_api.repository;

import java.util.List;
import java.util.Optional;

import com.example.learning_management_system_api.utils.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.learning_management_system_api.entity.User;
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByRole(UserRole role, Pageable pageable);

    List<User> findByRole(UserRole role);



    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    Optional<User> findByVerificationCode(String verificationCode);

    Optional<User> findByRefreshToken(String refreshToken);
}
