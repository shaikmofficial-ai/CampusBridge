// MentorProfileRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.MentorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {
    Optional<MentorProfile> findByUser_Id(Long userId);
    @Query("SELECT m FROM MentorProfile m WHERE " +
            "(:domain IS NULL OR :domain MEMBER OF m.domains) AND " +
            "(:keyword IS NULL OR LOWER(m.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MentorProfile> searchMentors(String domain, String keyword);
}