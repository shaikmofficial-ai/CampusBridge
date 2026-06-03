package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.PlacementDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementDriveRepository extends JpaRepository<PlacementDrive, Long> {
    List<PlacementDrive> findByStatusOrderByApplicationDeadlineAsc(PlacementDrive.DriveStatus status);
    List<PlacementDrive> findAllByOrderByCreatedAtDesc();
    long countByStatus(PlacementDrive.DriveStatus status);
}