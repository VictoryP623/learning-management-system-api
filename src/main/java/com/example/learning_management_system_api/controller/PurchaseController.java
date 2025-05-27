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
// @CrossOrigin(origins = "http://localhost:3000")
public class PurchaseController {

  private final PurchaseService purchaseService;

  public PurchaseController(PurchaseService purchaseService) {
    this.purchaseService = purchaseService;
  }

  // 1. FE gọi API này để lấy link PayPal
  @PostMapping("/paypal")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<?> createPaypal(
      @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody List<Long> courseIds) {
    return ResponseEntity.ok(
        purchaseService.createPaypalPayment(userDetails.getUserId(), courseIds));
  }

  // 2. FE gọi API này sau khi PayPal redirect về (xác nhận giao dịch)
  @PostMapping("/paypal/execute")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<?> executePaypalPayment(@RequestBody Map<String, String> payload) {
    String paymentId = payload.get("paymentId");
    String payerId = payload.get("payerId");
    Long purchaseId = Long.parseLong(payload.get("purchaseId"));
    try {
      purchaseService.executePaypalPayment(paymentId, payerId, purchaseId);
      return ResponseEntity.ok("Thanh toán thành công!");
    } catch (Exception e) {
      return ResponseEntity.status(500).body("Thanh toán thất bại! " + e.getMessage());
    }
  }

  // Lấy danh sách purchase của user
  @GetMapping("")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<List<PurchaseResponseDto>> getPurchases() {
    return new ResponseEntity<>(purchaseService.getAllPurchase(), HttpStatus.OK);
  }

  // Lấy khóa học đã mua
  @GetMapping("/courses")
  @PreAuthorize("hasRole('ROLE_Student')")
  public ResponseEntity<PageDto> getBoughtCourse(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return new ResponseEntity<>(purchaseService.getBoughtCourse(page, size), HttpStatus.OK);
  }
}
