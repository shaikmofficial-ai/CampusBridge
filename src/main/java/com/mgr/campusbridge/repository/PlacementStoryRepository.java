package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.PlacementStory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlacementStoryRepository extends JpaRepository<PlacementStory, Long> {
    List<PlacementStory> findAllByOrderByCreatedAtDesc();
}