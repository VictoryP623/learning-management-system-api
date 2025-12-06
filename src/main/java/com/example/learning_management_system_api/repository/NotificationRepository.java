package com.example.learning_management_system_api.repository;

import com.example.learning_management_system_api.entity.Notification;
import com.example.learning_management_system_api.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

  long countByRecipientAndReadFlagFalse(User recipient);

  // Phân trang theo recipient (sort sẽ đi từ Pageable)
  Page<Notification> findByRecipient(User recipient, Pageable pageable);

  // Bulk mark-all-read nhanh gọn
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "update Notification n set n.readFlag = true where n.recipient = :recipient and n.readFlag ="
          + " false")
  int markAllReadByRecipient(User recipient);

  //  check idempotency key để chống gửi trùng
  boolean existsByIdempotencyKey(String idempotencyKey);
}
