package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.ExternalJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExternalJobRepository extends JpaRepository<ExternalJob, Long> {

    Optional<ExternalJob> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    List<ExternalJob> findTop100ByOrderByPostedAtDesc();

    @Query("""
            SELECT j FROM ExternalJob j
            WHERE (:query IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(j.company) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
            ORDER BY j.postedAt DESC
            """)
    List<ExternalJob> search(@Param("query") String query, @Param("location") String location);
}
