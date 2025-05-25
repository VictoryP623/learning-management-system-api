package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.AdminRegisterRequest;
import com.example.learning_management_system_api.dto.response.ResponseVO;
import com.example.learning_management_system_api.service.AdminRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminRegisterController {
  @Autowired private AdminRegisterService adminRegisterService;

  @PostMapping("/register")
  public ResponseVO<?> registerAdmin(@RequestBody AdminRegisterRequest request) {
    try {
      String msg =
          adminRegisterService.registerAdmin(
              request.getEmail(), request.getPassword(), request.getFullname());
      return ResponseVO.success(msg);
    } catch (Exception e) {
      return ResponseVO.error(400, e.getMessage());
    }
  }
}
