package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "mentor_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    @PrePersist
    public void onCreate() {
        this.requestedAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}