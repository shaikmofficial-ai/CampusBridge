package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private String targetType;
    private Long targetId;
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private LocalDateTime reportedAt;

    @PrePersist
    public void prePersist() {
        this.reportedAt = LocalDateTime.now();
        this.status = ReportStatus.OPEN;
    }

    public enum ReportStatus {
        OPEN, REVIEWED, DISMISSED
    }
}