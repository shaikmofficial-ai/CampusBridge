package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.Conversation;
import com.mgr.campusbridge.entity.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ConversationResponse {
    private Long id;
    private boolean isGroup;
    private String groupName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private List<ParticipantInfo> participants;

    @Data
    @Builder
    public static class ParticipantInfo {
        private Long id;
        private String name;
        private String profilePicture;
    }

    public static ConversationResponse from(Conversation c) {
        List<ParticipantInfo> participants = c.getParticipants().stream()
                .map(u -> ParticipantInfo.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .profilePicture(u.getProfilePictureUrl())
                        .build())
                .collect(Collectors.toList());
        return ConversationResponse.builder()
                .id(c.getId())
                .isGroup(c.isGroup())
                .groupName(c.getGroupName())
                .lastMessage(c.getLastMessage())
                .lastMessageAt(c.getLastMessageAt())
                .participants(participants)
                .build();
    }
}