package com.mgr.campusbridge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    /** University register/roll number. Unique when present. */
    @Column(unique = true)
    private String registerNumber;

    private String department;

    private String batch;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String linkedinUrl;

    private String githubUrl;

    private String portfolioUrl;

    private String profilePictureUrl;

    private int communityPoints = 0;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus = AccountStatus.PENDING;

    /** Access state for the ban system. Defaults to ACTIVE. */
    @Column(name = "account_state", length = 16, nullable = false)
    @Builder.Default
    private String accountState = "ACTIVE";

    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_achievements",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "achievement")
    private List<String> achievements = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AccountStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum Role {
        STUDENT,
        ALUMNI,
        MENTOR,
        ADMIN
    }
}