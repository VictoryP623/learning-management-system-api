package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.component.GoogleOAuthApiClient;
import com.example.learning_management_system_api.component.JwtUtils;
import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.request.RegisterRequest;
import com.example.learning_management_system_api.dto.request.UpdatePasswordRequest;
import com.example.learning_management_system_api.dto.response.GoogleUserInfo;
import com.example.learning_management_system_api.dto.response.LoginResponse;
import com.example.learning_management_system_api.entity.Instructor;
import com.example.learning_management_system_api.entity.Student;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.exception.AppException;
import com.example.learning_management_system_api.repository.InstructorRepository;
import com.example.learning_management_system_api.repository.StudentRepository;
import com.example.learning_management_system_api.repository.UserRepository;
import com.example.learning_management_system_api.utils.enums.UserRole;
import com.example.learning_management_system_api.utils.enums.UserStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

@Service
public class AuthenticationService implements IAuthenticationService {

  private static final SecureRandom secureRandom = new SecureRandom(); // CSPRNG
  private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

  @Autowired private UserRepository userRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private InstructorRepository instructorRepository;

  @Autowired private EmailService emailService;

  @Autowired private PasswordEncoder encoder;

  @Autowired private JwtUtils jwtUtils;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private GoogleOAuthApiClient googleOAuthApiClient;

  @Value("${app.CLIENT_URL:localhost}")
  private String CLIENT_URL;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String GOOGLE_CLIENT_ID;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String REDIRECT_URI;

  @Override
  public String register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AppException(409, "User already exists in the system");
    }

    User user = request.toUser();
    user.setPassword(encoder.encode(request.getPassword()));
    user.setStatus(UserStatus.NOT_VERIFY);
    user.setRole(UserRole.valueOf(request.getRole()));

    String token = generateVerificationToken();
    user.setVerificationCode(token);
    user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
    userRepository.save(user);

    // Todo: create Student or Instructor
    if (user.getRole() == UserRole.Student) {
      Student student = new Student();
      student.setUser(user);
      studentRepository.save(student);
    } else {
      Instructor instructor = new Instructor();
      instructor.setUser(user);
      instructorRepository.save(instructor);
    }

    try {
      com.google.firebase.auth.UserRecord.CreateRequest createRequest =
          new com.google.firebase.auth.UserRecord.CreateRequest()
              .setEmail(request.getEmail())
              .setPassword(request.getPassword())
              .setEmailVerified(false)
              .setDisplayName(user.getFullname());

      com.google.firebase.auth.UserRecord userRecord =
          com.google.firebase.auth.FirebaseAuth.getInstance().createUser(createRequest);
      System.out.println("Created user on Firebase Auth: " + userRecord.getUid());
    } catch (Exception ex) {
      System.out.println("Lỗi khi tạo user trên Firebase Auth: " + ex.getMessage());
    }

    String verifyLink = String.format("%s/verify-email?token=%s", CLIENT_URL, token);

    Context context = new Context();
    context.setVariable("email", request.getEmail());
    context.setVariable("verificationLink", verifyLink);

    emailService.sendHtmlEmail(
        request.getEmail(), "Confirm your email address", "verification-email", context);

    return "Registration successful. Please check your email to complete account verification.";
  }

  @Override
  public LoginResponse authenticate(String email, String password) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(404, "Account does not exist"));

    if (user.getStatus().equals(UserStatus.DEACTIVE)) {
      throw new AppException(
          403,
          "Incorrect password. Your account has been deactivated. Please verify your account.");
    }
    if (user.getStatus().equals(UserStatus.NOT_VERIFY)) {
      throw new AppException(
          403, "Your account has not been verified. Please verify your account.");
    }

    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(email, password));
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

      user.setFailedAttempts(0);
      String accessToken = jwtUtils.generateAccessToken(user);
      String refreshToken = jwtUtils.generateRefreshToken(user);

      user.setRefreshToken(refreshToken);
      userRepository.save(user);

      return new LoginResponse(accessToken, refreshToken);

    } catch (BadCredentialsException ex) {
      int failedAttempts = user.getFailedAttempts() + 1;
      user.setFailedAttempts(failedAttempts);

      if (failedAttempts >= 3) {
        user.setStatus(UserStatus.DEACTIVE);
        userRepository.save(user);
        throw new AppException(
            403, "Your account has been locked due to too many incorrect password attempts.");
      } else {
        userRepository.save(user);
        throw new AppException(
            401,
            "Incorrect password. You have entered the wrong password "
                + failedAttempts
                + " times. Your account will be deactivated if you enter the wrong password 3"
                + " times.");
      }
    }
  }

  public String generateVerificationToken() {
    byte[] randomBytes = new byte[24];
    secureRandom.nextBytes(randomBytes);
    return base64Encoder.encodeToString(randomBytes);
  }

  @Override
  public String verifyEmail(String token) {
    Optional<User> userOptional = userRepository.findByVerificationCode(token);
    User user =
        userOptional.orElseThrow(
            () -> new AppException(404, "Invalid verification code. Please try again"));

    if (user.getVerificationCodeExpiry() == null
        || user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
      throw new AppException(400, "Verification code has expired");
    }

    if (!user.getStatus().equals(UserStatus.NOT_VERIFY)) {
      throw new AppException(400, "Verification failed. Please try again");
    }
    if (user.getRole() == UserRole.Student) {
      user.setStatus(UserStatus.ACTIVE);
    } else {
      user.setStatus(UserStatus.DEACTIVE);
    }
    user.setVerificationCode(null);
    user.setVerificationCodeExpiry(null);
    userRepository.save(user);
    if (user.getRole() == UserRole.Instructor) {
      return "You have registered as an Instructor, we will review your information within 14"
          + " days.";
    }

    return "Verification successful";
  }

  @Override
  public LoginResponse refreshAccessToken(String refreshToken) {
    Optional<User> userOptional = userRepository.findByRefreshToken(refreshToken);
    User user =
        userOptional.orElseThrow(
            () -> new AppException(404, "Invalid verification code. Please try again"));

    if (!jwtUtils.validateToken(refreshToken)) {
      throw new AppException(401, "Invalid verification code. Please try again");
    }

    String newAccessToken = jwtUtils.generateAccessToken(user);

    return new LoginResponse(newAccessToken, refreshToken);
  }

  @Override
  public String sendConfirmationEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(404, "Account does not exist"));

    String token = generateVerificationToken();
    user.setVerificationCode(token);
    user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(24));
    userRepository.save(user);

    String verifyLink = String.format("%s/verify-email?token=%s", CLIENT_URL, token);

    Context context = new Context();
    context.setVariable("email", email);
    context.setVariable("verificationLink", verifyLink);

    emailService.sendHtmlEmail(email, "Confirm your email address", "verification-email", context);

    return "Verification email resent. Please check your email to complete account verification.";
  }

  @Override
  public String forgotPassword(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(404, "Account does not exist"));
    String token = generateVerificationToken();
    user.setVerificationCode(token);
    user.setVerificationCodeExpiry(LocalDateTime.now().plusHours(1));
    userRepository.save(user);

    String verifyLink = String.format("%s/auth/reset-password?token=%s", CLIENT_URL, token);
    Context context = new Context();
    context.setVariable("email", email);
    context.setVariable("verificationLink", verifyLink);

    emailService.sendHtmlEmail(email, "Reset Password", "reset-password-email", context);

    return "Please check your email to reset your account.";
  }

  public void syncPasswordWithFirebase(String email, String password, String displayName) {
    try {
      UserRecord userRecord;
      try {
        // Thử lấy user theo email
        userRecord = FirebaseAuth.getInstance().getUserByEmail(email);

        // Nếu tìm thấy thì update password
        UserRecord.UpdateRequest updateRequest =
            new UserRecord.UpdateRequest(userRecord.getUid()).setPassword(password);
        FirebaseAuth.getInstance().updateUser(updateRequest);

        System.out.println("Đã update password trên Firebase Auth cho user: " + email);

      } catch (com.google.firebase.auth.FirebaseAuthException e) {
        // Nếu lỗi là NOT_FOUND thì tạo user mới
        if ("USER_NOT_FOUND".equals(e.getErrorCode())) {
          com.google.firebase.auth.UserRecord.CreateRequest createRequest =
              new com.google.firebase.auth.UserRecord.CreateRequest()
                  .setEmail(email)
                  .setPassword(password)
                  .setEmailVerified(false)
                  .setDisplayName(displayName);
          FirebaseAuth.getInstance().createUser(createRequest);

          System.out.println("Tạo mới user trên Firebase Auth: " + email);
        } else {
          // Các lỗi khác log ra
          System.err.println("Lỗi Firebase Auth: " + e.getMessage());
        }
      }
    } catch (Exception e) {
      // Các lỗi khác log ra
      System.err.println("Lỗi đồng bộ Firebase Auth: " + e.getMessage());
    }
  }

  @Override
  public String updatePassword(String token, String password) {
    Optional<User> userOptional = userRepository.findByVerificationCode(token);
    User user =
        userOptional.orElseThrow(
            () -> new AppException(404, "Invalid verification code. Please try again"));

    if (user.getVerificationCodeExpiry() == null
        || user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
      throw new AppException(400, "Verification code has expired");
    }

    if (encoder.matches(password, user.getPassword())) {
      throw new AppException(400, "New password must not be the same as the current password.");
    }

    user.setPassword(encoder.encode(password));
    user.setVerificationCode(null);
    user.setVerificationCodeExpiry(null);
    userRepository.save(user);

    // --- Đồng bộ lên Firebase Auth, tự tạo user nếu chưa có ---
    syncPasswordWithFirebase(user.getEmail(), password, user.getFullname());

    return "Password reset successful";
  }

  @Override
  public String updatePassword(String email, UpdatePasswordRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new AppException(404, "Account does not exist"));

    if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new AppException(400, "Current password is incorrect");
    }

    if (encoder.matches(request.getNewPassword(), user.getPassword())) {
      throw new AppException(400, "New password must not be the same as the current password.");
    }

    user.setPassword(encoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // --- Đồng bộ lên Firebase Auth, tự tạo user nếu chưa có ---
    syncPasswordWithFirebase(user.getEmail(), request.getNewPassword(), user.getFullname());

    return "Password updated successfully";
  }

  @Override
  public String generateGoogleAuthUrl() {
    String googleAuthUrl =
        "https://accounts.google.com/o/oauth2/v2/auth?"
            + "client_id="
            + GOOGLE_CLIENT_ID
            + "&redirect_uri="
            + REDIRECT_URI
            + "&response_type=code"
            + "&scope=email%20profile";
    return googleAuthUrl;
  }

  @Override
  public LoginResponse handleGoogleCallback(String code, String role) {
    String accessToken = googleOAuthApiClient.exchangeCodeForAccessToken(code);
    GoogleUserInfo googleUserInfo = googleOAuthApiClient.fetchGoogleUserInfo(accessToken);

    Optional<User> existingUser = userRepository.findByEmail(googleUserInfo.getEmail());
    User user;
    if (existingUser.isPresent()) {
      user = existingUser.get();
      if (user.getGoogleId() == null) {
        user.setGoogleId(googleUserInfo.getSub());
        userRepository.save(user);
      }
    } else {
      user = new User();
      user.setEmail(googleUserInfo.getEmail());
      user.setGoogleId(googleUserInfo.getSub());
      user.setFullname(googleUserInfo.getName());
      user.setPassword("");
      user.setStatus(UserStatus.ACTIVE);
      user.setRole(UserRole.valueOf(role));
      userRepository.save(user);
      if (user.getRole() == UserRole.Student) {
        Student student = new Student();
        student.setUser(user);
        studentRepository.save(student);
      } else {
        Instructor instructor = new Instructor();
        instructor.setUser(user);
        instructorRepository.save(instructor);
      }
    }
    String jwtAccessToken = jwtUtils.generateAccessToken(user);
    String jwtRefreshToken = jwtUtils.generateRefreshToken(user);
    user.setRefreshToken(jwtRefreshToken);
    userRepository.save(user);

    return new LoginResponse(jwtAccessToken, jwtRefreshToken);
  }

  @Override
  public String logout(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new AppException(404, "Account does not exist"));
    user.setRefreshToken(null);
    userRepository.save(user);
    return "Logout successful!";
  }
}