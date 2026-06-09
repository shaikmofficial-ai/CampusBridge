package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {
    List<LearningModule> findAllByOrderByOrderIndexAsc();
    Optional<LearningModule> findByOrderIndex(int orderIndex);
    long count();
}
