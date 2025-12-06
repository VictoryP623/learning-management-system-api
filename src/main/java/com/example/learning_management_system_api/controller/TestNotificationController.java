package com.example.learning_management_system_api.controller;

import com.example.learning_management_system_api.config.CustomUserDetails;
import com.example.learning_management_system_api.dto.request.NotificationCreateRequest;
import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.service.NotificationService;
import com.example.learning_management_system_api.utils.enums.NotificationType;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    "/api/notifications-test") // ❗ đổi namespace test để không trùng với controller thật
@RequiredArgsConstructor
public class TestNotificationController {

  private final NotificationService notificationService;

  // ---- Body test đơn giản (map sang NotificationCreateRequest) ----
  @Data
  public static class TestSendRequest {
    private NotificationType type; // dùng enum của bạn
    private String title;
    private String message;
    private String linkUrl;
    // actorId optional nếu muốn set thủ công
    private Long actorId;
  }

  /** Gửi notif cho "current user" theo JWT */
  @PostMapping("/send")
  public NotificationResponse sendToCurrentUser(
      @AuthenticationPrincipal CustomUserDetails me, @RequestBody TestSendRequest req) {

    Long recipientId = me.getUserId();
    NotificationCreateRequest dto = new NotificationCreateRequest();
    dto.setRecipientId(recipientId);
    dto.setActorId(req.getActorId()); // có thể null
    dto.setType(req.getType());
    dto.setTitle(req.getTitle());
    dto.setMessage(req.getMessage());
    dto.setLinkUrl(req.getLinkUrl());

    return notificationService.createAndDispatch(dto);
  }

  /** Gửi notif cho userId chỉ định (ROLE_ADMIN) */
  @PostMapping("/send/{userId}")
  @PreAuthorize("hasRole('Admin')")
  public NotificationResponse sendToUser(
      @AuthenticationPrincipal CustomUserDetails me,
      @PathVariable Long userId,
      @RequestBody TestSendRequest req) {

    NotificationCreateRequest dto = new NotificationCreateRequest();
    dto.setRecipientId(userId);
    // set actor là admin hiện tại nếu không cung cấp actorId trong body
    dto.setActorId(Objects.requireNonNullElse(req.getActorId(), me.getUserId()));
    dto.setType(req.getType());
    dto.setTitle(req.getTitle());
    dto.setMessage(req.getMessage());
    dto.setLinkUrl(req.getLinkUrl());

    return notificationService.createAndDispatch(dto);
  }

  // ⚠️ Tuỳ chọn: Nếu bạn muốn test list/count/markRead qua namespace test,
  // giữ lại các method dưới đây. Nếu không cần, có thể xoá để tránh trùng chức năng với controller
  // thật.

  /** Danh sách theo người nhận (paging); service hiện tại chưa có filter read/type */
  @GetMapping
  public PageDto list(
      @AuthenticationPrincipal CustomUserDetails me,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return notificationService.listByRecipient(me.getUserId(), page, size);
  }

  /** Đếm chưa đọc (service hiện có: countUnread) */
  @GetMapping("/count")
  public long countUnread(@AuthenticationPrincipal CustomUserDetails me) {
    return notificationService.countUnread(me.getUserId());
  }

  /** Đánh dấu đã đọc 1 cái (kèm kiểm tra quyền sở hữu trong service) */
  @PatchMapping("/{id}/read")
  public void markRead(@AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
    notificationService.markReadForUser(id, me.getUserId());
  }

  /** Đánh dấu tất cả đã đọc cho current user */
  @PatchMapping("/read-all")
  public void markAllRead(@AuthenticationPrincipal CustomUserDetails me) {
    notificationService.markAllRead(me.getUserId());
  }

  @GetMapping("/api/debug/me")
  public Map<String, Object> me(@AuthenticationPrincipal CustomUserDetails me) {
    return Map.of(
        "userId", me.getUserId(),
        "username", me.getUsername(),
        "authorities", me.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
  }
}
