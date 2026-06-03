package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.MentorConnection;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MentorConnectionRepository extends JpaRepository<MentorConnection, Long> {

    List<MentorConnection> findByStudentAndStatus(User student, MentorConnection.Status status);
    List<MentorConnection> findByMentorAndStatus(User mentor, MentorConnection.Status status);
    Optional<MentorConnection> findByStudentAndMentor(User student, User mentor);
    boolean existsByStudentAndMentorAndStatus(User student, User mentor, MentorConnection.Status status);
    long countByStudentAndStatus(User student, MentorConnection.Status status);

    @Query("SELECT c FROM MentorConnection c WHERE c.student = :user AND c.status = 'ACCEPTED'")
    List<MentorConnection> findAcceptedConnectionsByStudent(@Param("user") User user);

    @Query("SELECT c FROM MentorConnection c WHERE c.mentor = :user AND c.status = 'PENDING'")
    List<MentorConnection> findPendingRequestsForMentor(@Param("user") User user);
}