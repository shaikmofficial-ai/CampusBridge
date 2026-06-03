package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.SendMessageRequest;
import com.mgr.campusbridge.dto.request.StartConversationRequest;
import com.mgr.campusbridge.dto.response.ConversationResponse;
import com.mgr.campusbridge.dto.response.MessageResponse;
import com.mgr.campusbridge.entity.Conversation;
import com.mgr.campusbridge.entity.Message;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.repository.ConversationRepository;
import com.mgr.campusbridge.repository.MessageRepository;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<ConversationResponse> getConversations(String email) {
        User user = findByEmail(email);
        return conversationRepository.findByParticipantOrderByLastMessageAtDesc(user)
                .stream().map(ConversationResponse::from).collect(Collectors.toList());
    }

    public List<MessageResponse> getMessages(String email, Long conversationId) {
        User user = findByEmail(email);
        Conversation conversation = findConversationById(conversationId);
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(user.getId()));
        if (!isParticipant) {
            throw new ResourceNotFoundException("Conversation not found");
        }
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream().map(MessageResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ConversationResponse startConversation(String email, StartConversationRequest request) {
        User sender = findByEmail(email);
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getRecipientId()));

        return conversationRepository.findDirectConversation(sender, recipient)
                .map(ConversationResponse::from)
                .orElseGet(() -> {
                    Conversation conversation = Conversation.builder()
                            .participants(List.of(sender, recipient))
                            .isGroup(false)
                            .build();
                    return ConversationResponse.from(conversationRepository.save(conversation));
                });
    }

    @Transactional
    public MessageResponse sendMessage(String email, SendMessageRequest request) {
        User sender = findByEmail(email);
        Conversation conversation = findConversationById(request.getConversationId());

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(sender.getId()));
        if (!isParticipant) {
            throw new ResourceNotFoundException("You are not part of this conversation");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .build();
        Message saved = messageRepository.save(message);

        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return MessageResponse.from(saved);
    }

    private Conversation findConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + id));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}