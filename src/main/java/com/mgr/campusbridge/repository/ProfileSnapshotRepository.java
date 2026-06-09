package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.ProfileSnapshot;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProfileSnapshotRepository extends JpaRepository<ProfileSnapshot, Long> {

    /** Oldest -> newest, for charting. */
    List<ProfileSnapshot> findByUser_IdOrderByRecordedDateAsc(Long userId);

    /** One row per calendar day per user (so repeated edits update the same day). */
    Optional<ProfileSnapshot> findByUser_IdAndRecordedDate(Long userId, LocalDate recordedDate);
}
