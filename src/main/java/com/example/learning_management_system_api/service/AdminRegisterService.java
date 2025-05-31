package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.entity.Admin;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.repository.AdminRepository;
import com.example.learning_management_system_api.repository.UserRepository;
import com.example.learning_management_system_api.utils.enums.UserRole;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminRegisterService {
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder encoder;

  @Autowired
  private AdminRepository
      adminRepository; // Bạn cần tạo interface này extends JpaRepository<Admin, Long>

  public String registerAdmin(String email, String password, String fullname) {
    if (userRepository.existsByEmail(email)) {
      throw new RuntimeException("Tài khoản đã tồn tại trong hệ thống");
    }
    try {
      UserRecord.CreateRequest createRequest =
          new UserRecord.CreateRequest()
              .setEmail(email)
              .setPassword(password)
              .setDisplayName(fullname)
              .setEmailVerified(true);

      FirebaseAuth.getInstance().createUser(createRequest);

      User adminUser = new User();
      adminUser.setEmail(email);
      adminUser.setPassword(encoder.encode(password));
      adminUser.setFullname(fullname);
      adminUser.setRole(UserRole.Admin);
      adminUser.setStatus(UserStatus.ACTIVE);

      userRepository.save(adminUser);

      // *** Bổ sung đoạn này ***
      Admin admin = new Admin();
      admin.setUser(adminUser); // set reference
      adminRepository.save(admin);

      return "Admin registered successfully! (Email: " + email + ")";
    } catch (Exception e) {
      throw new RuntimeException("Lỗi khi đăng ký admin: " + e.getMessage());
    }
  }
}
