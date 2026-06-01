// ForumCommentRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPostIdOrderByCreatedAtAsc(Long postId);
}