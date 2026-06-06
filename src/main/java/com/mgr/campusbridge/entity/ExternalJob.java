package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A job opening fetched from an external provider (e.g. Adzuna) and cached
 * locally so the frontend can display it without calling the provider on every
 * page load (keeps us within free-tier limits).
 */
@Entity
@Table(name = "external_jobs", indexes = {
        @Index(name = "idx_external_jobs_external_id", columnList = "externalId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Provider's unique id for this posting (used to de-duplicate on refresh). */
    @Column(nullable = false, unique = true)
    private String externalId;

    @Column(nullable = false)
    private String title;

    private String company;
    private String location;
    private String category;

    private Double salaryMin;
    private Double salaryMax;

    /** e.g. "full_time" / "part_time" when the provider supplies it. */
    private String contractTime;

    @Column(length = 1024)
    private String redirectUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Provider name, e.g. "Adzuna". */
    private String source;

    private LocalDateTime postedAt;
    private LocalDateTime fetchedAt;
}
