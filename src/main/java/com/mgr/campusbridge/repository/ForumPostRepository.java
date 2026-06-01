// ForumPostRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByIsPublicTrueOrderByCreatedAtDesc();
    List<ForumPost> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    long countByAuthorId(Long userId);
}