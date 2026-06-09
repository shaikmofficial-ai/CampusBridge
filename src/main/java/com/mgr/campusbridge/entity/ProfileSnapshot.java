package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * A point-in-time record of a student's Placement Readiness Index (PRI),
 * used to graph their growth curve over time.
 */
@Entity
@Table(name = "profile_snapshots", indexes = {
        @Index(name = "idx_snapshot_user_date", columnList = "user_id,recorded_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(nullable = false)
    private int score;
}
