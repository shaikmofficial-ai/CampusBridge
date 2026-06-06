package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A job/internship opening posted by a mentor for students. */
@Entity
@Table(name = "mentor_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String company;
    private String location;

    /** e.g. FULL_TIME, INTERNSHIP, PART_TIME, CONTRACT. */
    private String jobType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String applyLink;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mentor_job_skills", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "posted_by")
    private User postedBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
