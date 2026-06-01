package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "placement_stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String story;

    private String excerpt;
    private LocalDateTime postedAt;

    @PrePersist
    public void prePersist() {
        this.postedAt = LocalDateTime.now();
    }
}