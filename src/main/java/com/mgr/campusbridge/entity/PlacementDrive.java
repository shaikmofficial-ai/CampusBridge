package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "placement_drives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlacementDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String role;
    private String package_;
    private String eligibleBatch;
    private LocalDate lastDate;

    @Enumerated(EnumType.STRING)
    private DriveStatus status;

    public enum DriveStatus {
        OPEN, CLOSING, CLOSED
    }
}