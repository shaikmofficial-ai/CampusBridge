package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "mentor_id")
    private User mentor;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime connectedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}