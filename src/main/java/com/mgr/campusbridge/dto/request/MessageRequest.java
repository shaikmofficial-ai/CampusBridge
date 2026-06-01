// MessageRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
@Data
public class MessageRequest {
    private Long conversationId;
    private String content;
}