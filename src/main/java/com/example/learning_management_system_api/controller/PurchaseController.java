package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.dto.response.PurchaseResponseDto;
import com.example.learning_management_system_api.service.PurchaseService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/purchases")
//@CrossOrigin(origins = "http://localhost:3000")
public class PurchaseController {

  private final PurchaseService purchaseService;

  public PurchaseController(PurchaseService purchaseService) {
    this.purchaseService = purchaseService;
  }

  @PostMapping("")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<Object> initPurchase(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return new ResponseEntity<>(purchaseService.initPurchase(userDetails.getUserId()), HttpStatus.OK);
  }

  @GetMapping("/callback")
  public ResponseEntity<String> handleMomoCallback(@RequestParam Map<String, String> params) {
    return purchaseService.handleMomoCallback(params);
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<List<PurchaseResponseDto>> getPurchases() {
    return new ResponseEntity<>(purchaseService.getAllPurchase(), HttpStatus.OK);
  }

  @GetMapping("/courses")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<PageDto> getBoughtCourse(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
    return new ResponseEntity<>(purchaseService.getBoughtCourse(page, size), HttpStatus.OK);
  }
}
