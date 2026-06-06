package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.ForumComment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ForumCommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String authorProfilePictureUrl;

    public static ForumCommentResponse from(ForumComment c) {
        return ForumCommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .authorId(c.getAuthor() != null ? c.getAuthor().getId() : null)
                .authorName(c.getAuthor() != null ? c.getAuthor().getName() : "Unknown")
                .authorRole(c.getAuthor() != null && c.getAuthor().getRole() != null
                        ? c.getAuthor().getRole().name() : null)
                .authorProfilePictureUrl(c.getAuthor() != null ? c.getAuthor().getProfilePictureUrl() : null)
                .build();
    }
}
