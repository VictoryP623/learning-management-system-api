package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.request.GoogleSSORequest;
import com.example.learning_management_system_api.dto.request.LoginRequest;
import com.example.learning_management_system_api.dto.request.RegisterRequest;
import com.example.learning_management_system_api.dto.request.ResetPasswordRequest;
import com.example.learning_management_system_api.dto.request.TokenRefreshRequest;
import com.example.learning_management_system_api.dto.request.UpdatePasswordRequest;
import com.example.learning_management_system_api.dto.response.LoginResponse;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.service.IAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthenticateController {
    @Autowired
    private IAuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseVO<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseVO.success(authenticationService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseVO<?> forgotPassword(@Valid @RequestParam("email") String email) {
        String message = authenticationService.forgotPassword(email);
        return ResponseVO.success(message);
    }

    @PutMapping("/reset-password")
    public ResponseVO<?> resetPassword(
            @Valid @RequestParam("token") String token, @Valid @RequestBody ResetPasswordRequest request) {
        String message = authenticationService.updatePassword(token, request.getPassword());
        return ResponseVO.success(message);
    }

    @PutMapping("/update-password")
    @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
    public ResponseVO<?> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = authenticationService.updatePassword(userDetails.getUsername(), request);
        return ResponseVO.success(message);


    }

    @PostMapping("/login")
    public ResponseVO<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseVO.success(
                authenticationService.authenticate(request.getEmail(), request.getPassword()));
    }

    @GetMapping("/verify")
    public ResponseVO<?> verifyEmail(@Valid @RequestParam("token") String token) {
        String message = authenticationService.verifyEmail(token);
        return ResponseVO.success(message);
    }

    @PostMapping("/resend-confirmation")
    public ResponseVO<?> reSendConfirmationEmail(@Valid @RequestParam("email") String email) {
        String message = authenticationService.sendConfirmationEmail(email);
        return ResponseVO.success(message);
    }

    @PostMapping("/refresh-token")
    public ResponseVO<LoginResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseVO.success(authenticationService.refreshAccessToken(request.getRefreshToken()));
    }

    @GetMapping("/google")
    public ResponseVO<String> getGoogleAuthUrl() {
        String googleAuthUrl = authenticationService.generateGoogleAuthUrl();
        return ResponseVO.success(googleAuthUrl);
    }

    @PostMapping("/google/callback")
    public ResponseVO<LoginResponse> handleGoogleCallback(
            @Valid @RequestBody GoogleSSORequest request) {
        return ResponseVO.success(
                authenticationService.handleGoogleCallback(request.getCode(), request.getRole()));
    }

    @GetMapping("/logout")
    public ResponseVO<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = authenticationService.logout(userDetails.getUserId());
        return ResponseVO.success(message);
    }
}
