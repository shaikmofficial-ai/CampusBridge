package com.mgr.campusbridge.service;

import com.mgr.campusbridge.entity.*;
import com.mgr.campusbridge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public List<Conversation> getUserConversations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return conversationRepository.findByParticipant(user);
    }

    public List<Message> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    public Message sendMessage(String senderEmail, Long conversationId, String content) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setLastMessage(content);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return messageRepository.save(Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .build());
    }

    public Conversation startConversation(String email, Long otherUserId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = Conversation.builder()
                .participants(List.of(user, other))
                .isGroup(false)
                .lastMessageAt(LocalDateTime.now())
                .build();
        return conversationRepository.save(conversation);
    }
}