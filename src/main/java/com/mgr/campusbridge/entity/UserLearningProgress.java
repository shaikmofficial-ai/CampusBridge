package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Maps a user to a learning module they have completed. */
@Entity
@Table(name = "user_learning_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_module", columnNames = {"user_id", "module_order_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLearningProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** The orderIndex of the completed module. */
    @Column(name = "module_order_id", nullable = false)
    private int moduleOrderId;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {
        this.completedAt = LocalDateTime.now();
    }
}
