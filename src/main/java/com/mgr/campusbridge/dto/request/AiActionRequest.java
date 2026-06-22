package com.mgr.campusbridge.dto.request;

import lombok.Data;

@Data
public class AiActionRequest {
    /** resume | roadmap | interview | internships | skills | chat */
    private String actionType;
    /** Free-text message (used for the chat action). */
    private String message;
}
