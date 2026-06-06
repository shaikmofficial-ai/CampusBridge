package com.mgr.campusbridge.service;

import com.mgr.campusbridge.dto.request.SendMessageRequest;
import com.mgr.campusbridge.dto.request.StartConversationRequest;
import com.mgr.campusbridge.dto.response.ConversationResponse;
import com.mgr.campusbridge.dto.response.MessageResponse;
import com.mgr.campusbridge.entity.Conversation;
import com.mgr.campusbridge.entity.MentorConnection;
import com.mgr.campusbridge.entity.Message;
import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.exception.ResourceNotFoundException;
import com.mgr.campusbridge.exception.UnauthorizedException;
import com.mgr.campusbridge.repository.ConversationRepository;
import com.mgr.campusbridge.repository.MentorConnectionRepository;
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
    private final MentorConnectionRepository mentorConnectionRepository;

    public List<ConversationResponse> getConversations(String email) {
        User user = findByEmail(email);
        requireApproved(user);
        return conversationRepository.findByParticipantOrderByLastMessageAtDesc(user)
                .stream().map(ConversationResponse::from).collect(Collectors.toList());
    }

    public List<MessageResponse> getMessages(String email, Long conversationId) {
        User user = findByEmail(email);
        requireApproved(user);
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
        requireApproved(sender);
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getRecipientId()));

        requireConnected(sender, recipient);

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
        requireApproved(sender);
        Conversation conversation = findConversationById(request.getConversationId());

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(sender.getId()));
        if (!isParticipant) {
            throw new ResourceNotFoundException("You are not part of this conversation");
        }

        // For 1:1 conversations, ensure an accepted mentor connection still exists.
        if (!conversation.isGroup()) {
            conversation.getParticipants().stream()
                    .filter(p -> !p.getId().equals(sender.getId()))
                    .findFirst()
                    .ifPresent(other -> requireConnected(sender, other));
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

    /** Block users whose account is not yet approved by an admin (admins bypass). */
    private void requireApproved(User user) {
        if (user.getRole() == User.Role.ADMIN) return;
        if (user.getAccountStatus() != User.AccountStatus.APPROVED) {
            throw new UnauthorizedException(
                    "Your account is pending admin approval. You can chat once an admin approves you.");
        }
    }

    /**
     * Only users with an ACCEPTED mentor connection may message each other.
     * Admins can message anyone (e.g. for moderation/support).
     */
    private void requireConnected(User a, User b) {
        if (a.getRole() == User.Role.ADMIN || b.getRole() == User.Role.ADMIN) return;
        boolean connected =
                mentorConnectionRepository.existsByStudentAndMentorAndStatus(a, b, MentorConnection.Status.ACCEPTED)
             || mentorConnectionRepository.existsByStudentAndMentorAndStatus(b, a, MentorConnection.Status.ACCEPTED);
        if (!connected) {
            throw new UnauthorizedException(
                    "You can only message someone you're connected with. Send a connection request and wait for it to be accepted.");
        }
    }
}