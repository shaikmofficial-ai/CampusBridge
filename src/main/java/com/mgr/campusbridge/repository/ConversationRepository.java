// ConversationRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.Conversation;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByParticipant(User user);
}