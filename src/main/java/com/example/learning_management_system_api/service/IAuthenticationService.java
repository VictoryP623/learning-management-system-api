package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.RegisterRequest;
import com.example.learning_management_system_api.dto.request.UpdatePasswordRequest;
import com.example.learning_management_system_api.dto.response.LoginResponse;
import org.springframework.stereotype.Service;

@Service
public interface IAuthenticationService {
  public String register(RegisterRequest request);

  public String sendConfirmationEmail(String email);

  public String verifyEmail(String token);

  public LoginResponse refreshAccessToken(String refreshToken);

  public LoginResponse authenticate(String email, String password);

  public String forgotPassword(String email);

  public String updatePassword(String token, String password);

  public String updatePassword(String email, UpdatePasswordRequest request);

  public String generateGoogleAuthUrl();

  public String logout(Long userId);

  public LoginResponse handleGoogleCallback(String code, String role);
}
