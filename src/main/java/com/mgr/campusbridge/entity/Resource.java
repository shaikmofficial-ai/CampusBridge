package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private ResourceType type;

    private String department;
    private String filePath;
    private String fileSize;
    private int downloadCount;
    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
        this.downloadCount = 0;
    }

    public enum ResourceType {
        NOTES, VIDEO, BOOK, TEMPLATE, QUESTION_BANK
    }
}