package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.MentorPlacement;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorPlacementRepository extends JpaRepository<MentorPlacement, Long> {
    List<MentorPlacement> findByMentorOrderByCreatedAtDesc(User mentor);
    List<MentorPlacement> findByMentor_IdOrderByCreatedAtDesc(Long mentorId);
    long countByMentor_Id(Long mentorId);
    long countByMentor(User mentor);
}
