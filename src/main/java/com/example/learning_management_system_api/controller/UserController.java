package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.UpdateUserStatusRequest;
import com.example.learning_management_system_api.dto.request.UserRequestDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.dto.response.UserResponseDto;
import com.example.learning_management_system_api.service.UserService;
import com.example.learning_management_system_api.utils.enums.UserRole;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PatchMapping("/users/{id}")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor')")
  public ResponseEntity<UserResponseDto> updateUser(
      @PathVariable Long id, @RequestBody @Valid UserRequestDto userRequestDto) {
    return new ResponseEntity<>(userService.updateUser(id, userRequestDto), HttpStatus.OK);
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<PageDto> getUsersByRole(
      @RequestParam(required = false) UserRole role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "5") int size) {
    if (role == null) {
      return ResponseVO.success(
          "Success!", userService.getUsers(PageRequest.of(page, size))); // Fetch all users
    } else {
      return ResponseVO.success(
          "Success!",
          userService.getUsersByRole(role, PageRequest.of(page, size))); // Fetch users by role
    }
  }

  @PatchMapping("admin/users/{id}/status")
  @PreAuthorize("hasRole('ROLE_Admin')")
  public ResponseVO<UserResponseDto> updateUserStatus(
      @RequestBody UpdateUserStatusRequest request, @PathVariable Long id) {
    UserResponseDto updatedUser = userService.updateUserStatus(request, id);
    return ResponseVO.success(updatedUser);
  }


}
