package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.Message;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderPicture;
    private String content;
    private boolean isRead;
    private LocalDateTime sentAt;

    public static MessageResponse from(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .senderPicture(m.getSender().getProfilePictureUrl())
                .content(m.getContent())
                .isRead(m.isRead())
                .sentAt(m.getSentAt())
                .build();
    }
}