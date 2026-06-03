package com.mgr.campusbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartConversationRequest {

    @NotNull(message = "Recipient user ID is required")
    private Long recipientId;
}