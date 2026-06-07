package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Records a student who was placed under a specific mentor's guidance.
 * Powers the "Students Placed Under Guidance" section on a mentor profile.
 */
@Entity
@Table(name = "mentor_placements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorPlacement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The mentor who guided the student. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    /** Optional link to a registered student account. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    /** Display fields (kept denormalized so records survive even without a linked account). */
    @Column(nullable = false)
    private String studentName;

    private String batch;

    @Column(nullable = false)
    private String company;

    private String role;
    private String packageAmount;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
