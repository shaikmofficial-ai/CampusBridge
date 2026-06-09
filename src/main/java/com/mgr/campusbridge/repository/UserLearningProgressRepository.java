package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.UserLearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserLearningProgressRepository extends JpaRepository<UserLearningProgress, Long> {
    List<UserLearningProgress> findByUserId(Long userId);
    boolean existsByUserIdAndModuleOrderId(Long userId, int moduleOrderId);
    long countByUserId(Long userId);

    /** [userId, lessonCount] pairs for leaderboard aggregation. */
    @Query("SELECT p.userId, COUNT(p) FROM UserLearningProgress p GROUP BY p.userId")
    List<Object[]> countLessonsPerUser();
}
