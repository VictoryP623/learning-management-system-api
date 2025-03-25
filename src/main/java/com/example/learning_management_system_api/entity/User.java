package com.example.learning_management_system_api.entity;

import com.example.learning_management_system_api.utils.enums.UserRole;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "`user`")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  private String googleId;

  private String password;

  private LocalDateTime birthdate;

  private String address;

  private String fullname;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(name = "refresh_token")
  private String refreshToken;

  @Column(name = "verification_code", length = 64)
  private String verificationCode;

  @Column(name = "verification_code_expiry")
  private LocalDateTime verificationCodeExpiry;

  @Column(columnDefinition = "TINYINT")
  private UserStatus status;

  @Column(name = "failed_attempts", nullable = false)
  private int failedAttempts = 0;

  @CreationTimestamp private LocalDateTime createdAt;

  @UpdateTimestamp private LocalDateTime updatedAt;
}
