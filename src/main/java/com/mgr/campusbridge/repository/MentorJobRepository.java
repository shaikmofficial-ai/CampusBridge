package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.MentorJob;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorJobRepository extends JpaRepository<MentorJob, Long> {
    List<MentorJob> findAllByOrderByCreatedAtDesc();
    List<MentorJob> findByPostedByOrderByCreatedAtDesc(User postedBy);
}
