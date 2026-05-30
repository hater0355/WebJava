package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByAccountUsernameOrderByCreatedAtDesc(String accountUsername);
    Optional<Notification> findByAccountUsernameAndMessage(String accountUsername, String message);
}
