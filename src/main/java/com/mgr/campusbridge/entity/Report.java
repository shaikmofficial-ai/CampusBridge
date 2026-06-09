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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @Column(nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** What kind of thing is being reported. */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 32)
    private TargetType targetType;

    /** Id of the reported content (forum post / resource), when applicable. */
    private Long targetId;

    /** Snapshot of the content title for the admin queue (content may be deleted). */
    private String targetTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private ReportStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = ReportStatus.OPEN;
        if (this.targetType == null) this.targetType = TargetType.USER;
    }

    public enum ReportStatus {
        OPEN, RESOLVED, DISMISSED
    }

    public enum TargetType {
        USER, FORUM_POST, RESOURCE
    }
}