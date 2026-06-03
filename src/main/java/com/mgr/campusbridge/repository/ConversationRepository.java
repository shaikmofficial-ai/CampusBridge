package com.mgr.campusbridge.repository;

import com.mgr.campusbridge.entity.Conversation;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByParticipantOrderByLastMessageAtDesc(@Param("user") User user);

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE p1 = :user1 AND p2 = :user2 AND c.isGroup = false")
    Optional<Conversation> findDirectConversation(@Param("user1") User user1,
                                                  @Param("user2") User user2);
}