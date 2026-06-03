package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_resources",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resource_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    private LocalDateTime savedAt;

    @PrePersist
    public void onCreate() {
        this.savedAt = LocalDateTime.now();
    }
}