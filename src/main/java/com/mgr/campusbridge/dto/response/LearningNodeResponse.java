package com.mgr.campusbridge.dto.response;

import com.mgr.campusbridge.entity.LearningModule;
import lombok.Builder;
import lombok.Data;

/** A roadmap node with the caller's per-node state (locked/active/completed). */
@Data
@Builder
public class LearningNodeResponse {
    private Long id;
    private int orderIndex;
    private String title;
    private String content;
    private String mission;
    private String starterCode;
    private int languageId;
    private String language;
    /** LOCKED | ACTIVE | COMPLETED */
    private String state;

    public static LearningNodeResponse from(LearningModule m, String state) {
        return LearningNodeResponse.builder()
                .id(m.getId())
                .orderIndex(m.getOrderIndex())
                .title(m.getTitle())
                .content(m.getContent())
                .mission(m.getMission())
                .starterCode(m.getStarterCode())
                .languageId(m.getLanguageId())
                .language(m.getLanguage())
                .state(state)
                .build();
    }
}
