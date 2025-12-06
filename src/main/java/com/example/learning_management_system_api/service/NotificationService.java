package com.example.learning_management_system_api.service;

import com.example.learning_management_system_api.dto.request.NotificationCreateRequest;
import com.example.learning_management_system_api.dto.response.NotificationResponse;
import com.example.learning_management_system_api.dto.response.PageDto;
import com.example.learning_management_system_api.entity.Notification;
import com.example.learning_management_system_api.entity.User;
import com.example.learning_management_system_api.repository.NotificationRepository;
import com.example.learning_management_system_api.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepo;
  private final UserRepository userRepo;
  private final SimpMessagingTemplate messaging;

  public NotificationService(
      NotificationRepository notificationRepo,
      UserRepository userRepo,
      SimpMessagingTemplate messaging) {
    this.notificationRepo = notificationRepo;
    this.userRepo = userRepo;
    this.messaging = messaging;
  }

  // ========================
  // 1. LIST & COUNT
  // ========================

  public PageDto listByRecipient(Long recipientId, int page, int size) {
    User recipient = userRepo.findById(recipientId).orElseThrow();
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Notification> p = notificationRepo.findByRecipient(recipient, pageable);

    List<NotificationResponse> content = p.getContent().stream().map(this::toResponse).toList();

    return new PageDto(
        p.getNumber(),
        p.getSize(),
        p.getTotalPages(),
        p.getTotalElements(),
        (List<Object>) (List<?>) content);
  }

  public long countUnread(Long recipientId) {
    User recipient = userRepo.findById(recipientId).orElseThrow();
    return notificationRepo.countByRecipientAndReadFlagFalse(recipient);
  }

  // ========================
  // 2. CREATE FROM API (Manual/Test)
  // ========================

  @Transactional
  public NotificationResponse createAndDispatch(NotificationCreateRequest req) {
    User recipient = userRepo.findById(req.getRecipientId()).orElseThrow();
    User actor = null;
    if (req.getActorId() != null) {
      actor = userRepo.findById(req.getActorId()).orElse(null);
    }

    // BẮT BUỘC: topic & idempotencyKey để thỏa NOT NULL trong entity & DB
    // Dùng topic "tạm" chỉ để test thủ công qua API.
    var topicForTest =
        com.example.learning_management_system_api.utils.enums.NotificationTopic
            .COURSE_STATUS_CHANGED;
    var idem = "manual:" + java.util.UUID.randomUUID();

    Notification n =
        Notification.builder()
            .recipient(recipient)
            .actor(actor)
            .type(req.getType())
            .topic(topicForTest) // <-- BẮT BUỘC
            .idempotencyKey(idem) // <-- BẮT BUỘC
            .title(req.getTitle())
            .message(req.getMessage())
            .linkUrl(req.getLinkUrl())
            .build();

    Notification saved = notificationRepo.save(n);
    NotificationResponse payload = toResponse(saved);

    // Gửi realtime tới user-queue cho KHỚP FE:
    // FE subscribe: /user/queue/notifications
    messaging.convertAndSendToUser(
        String.valueOf(recipient.getId()), "/queue/notifications", payload);

    return payload;
  }

  // ========================
  // 3. MARK READ
  // ========================

  @Transactional
  public void markRead(Long id) {
    Notification n = notificationRepo.findById(id).orElseThrow();
    if (!n.isReadFlag()) {
      n.setReadFlag(true);
      notificationRepo.save(n);
    }
  }

  @Transactional
  public void markAllRead(Long recipientId) {
    User recipient = userRepo.findById(recipientId).orElseThrow();
    notificationRepo.markAllReadByRecipient(recipient);
  }

  @Transactional
  public void markReadForUser(Long notificationId, Long currentUserId) {
    Notification n = notificationRepo.findById(notificationId).orElseThrow();
    if (n.getRecipient() == null || !n.getRecipient().getId().equals(currentUserId)) {
      throw new AccessDeniedException("Not your notification");
    }
    if (!n.isReadFlag()) {
      n.setReadFlag(true);
      notificationRepo.save(n);
    }
  }

  // ========================
  // 4. DÙNG CHO DOMAIN NOTIFIER (Backend event)
  // ========================

  @Transactional
  public Notification save(Notification notification) {
    return notificationRepo.save(notification);
  }

  // ========================
  // 5. MAPPING DTO
  // ========================

  private NotificationResponse toResponse(Notification n) {
    return NotificationResponse.builder()
        .id(n.getId())
        .recipientId(n.getRecipient() != null ? n.getRecipient().getId() : null)
        .actorId(n.getActor() != null ? n.getActor().getId() : null)
        .type(n.getType())
        .title(n.getTitle())
        .message(n.getMessage())
        .linkUrl(n.getLinkUrl())
        .readFlag(n.isReadFlag())
        .createdAt(n.getCreatedAt())
        .build();
  }
}
