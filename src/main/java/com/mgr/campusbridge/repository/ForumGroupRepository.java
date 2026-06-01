// ForumGroupRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.ForumGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ForumGroupRepository extends JpaRepository<ForumGroup, Long> {
    List<ForumGroup> findByIsPrivateFalse();
    List<ForumGroup> findByIsPrivateTrue();
}