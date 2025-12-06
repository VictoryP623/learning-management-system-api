package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.dto.request.NotificationCreateRequest;
import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService service;

  public NotificationController(NotificationService service) {
    this.service = service;
  }

  @GetMapping
  public PageDto list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Authentication auth) {
    Long rid = resolveRecipientId(auth);
    return service.listByRecipient(rid, page, size);
  }

  /** Đếm số chưa đọc của user hiện tại */
  @GetMapping("/unread-count")
  public long unreadCount(Authentication auth) {
    Long rid = resolveRecipientId(auth);
    return service.countUnread(rid);
  }

  /** Đánh dấu 1 thông báo đã đọc (thuộc user hiện tại) */
  @PostMapping("/mark-read/{id}")
  public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
    Long rid = resolveRecipientId(auth);
    service.markReadForUser(id, rid); // check ownership trong service
    return ResponseEntity.ok().build();
  }

  /** Đánh dấu tất cả đã đọc cho user hiện tại */
  @PostMapping("/mark-all-read")
  public ResponseEntity<Void> markAllRead(Authentication auth) {
    Long rid = resolveRecipientId(auth);
    service.markAllRead(rid);
    return ResponseEntity.ok().build();
  }

  /** Tạo thông báo & bắn realtime (admin/job/test) – vẫn nhận recipientId trong request */
  @PostMapping("/test-send")
  public NotificationResponse testSend(@Valid @RequestBody NotificationCreateRequest req) {
    return service.createAndDispatch(req);
  }

  /** Lấy userId từ CustomUserDetails (JWT) */
  private Long resolveRecipientId(Authentication auth) {
    if (auth == null || auth.getPrincipal() == null) {
      throw new UnauthorizedException("Unauthenticated");
    }

    Object principal = auth.getPrincipal();

    if (principal
        instanceof com.example.learning_management_system_api.config.CustomUserDetails cud) {
      return cud.getUserId();
    }

    throw new UnauthorizedException("Principal is not CustomUserDetails");
  }

  @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
  private static class UnauthorizedException extends RuntimeException {
    UnauthorizedException(String msg) {
      super(msg);
    }
  }
}