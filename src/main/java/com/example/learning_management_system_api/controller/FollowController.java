package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.request.FollowRequestDto;
import com.example.learning_management_system_api.dto.response.FollowResponseDto;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
//@CrossOrigin(origins = "http://localhost:3000")
public class FollowController {

  @Autowired private IFollowService followService;

  @PostMapping("")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<FollowResponseDto> addFollow(
      @RequestBody FollowRequestDto followRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null || userDetails.getUserId() == null) {
      throw new IllegalStateException("Authenticated user is null");
    }

    Long userId = userDetails.getUserId();
    // Long userId = 2L;
    if (followRequestDto.instructorId() == null) {
      throw new IllegalArgumentException("Instructor ID cannot be null");
    }
    FollowResponseDto response = followService.addFollow(userId, followRequestDto.instructorId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/instructors")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getFollowedInstructors(
      @RequestParam Long studentId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageDto instructors = followService.getFollowedInstructors(studentId, page, size);
    return ResponseEntity.ok(instructors);
  }

  @GetMapping("/students")
  @PreAuthorize("hasRole('ROLE_Student') or hasRole('ROLE_Instructor') or hasRole('ROLE_Admin')")
  public ResponseEntity<PageDto> getFollowedStudents(
      @RequestParam Long instructorId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageDto students = followService.getFollowedStudents(instructorId, page, size);
    return ResponseEntity.ok(students);
  }

  @DeleteMapping("")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<?> deleteFollow(
      @RequestBody FollowRequestDto followRequestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    //        CustomUserDetails userDetails = (CustomUserDetails)
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (userDetails == null || userDetails.getUserId() == null) {
      throw new IllegalStateException("Authenticated user is null");
    }
    if (followRequestDto.instructorId() == null) {
      throw new IllegalArgumentException("Instructor ID cannot be null");
    }

    Long userId = userDetails.getUserId();
    followService.deleteFollow(userId, followRequestDto.instructorId());
    return ResponseEntity.ok("Unfollowed successfully.");
  }
}
