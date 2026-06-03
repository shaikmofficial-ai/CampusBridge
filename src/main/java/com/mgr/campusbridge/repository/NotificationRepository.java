package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.Notification;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndIsRead(User user, boolean isRead);
}