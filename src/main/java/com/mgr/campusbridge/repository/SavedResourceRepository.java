package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.Resource;
import com.mgr.campusbridge.entity.SavedResource;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedResourceRepository extends JpaRepository<SavedResource, Long> {
    Optional<SavedResource> findByUserAndResource(User user, Resource resource);
    boolean existsByUserAndResource(User user, Resource resource);
    long countByUser(User user);

    @Query("SELECT sr.resource FROM SavedResource sr WHERE sr.user = :user ORDER BY sr.savedAt DESC")
    List<Resource> findSavedResourcesByUser(@Param("user") User user);
}