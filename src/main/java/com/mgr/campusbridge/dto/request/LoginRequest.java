// LoginRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
@Data
public class LoginRequest {
    /** Email (legacy) — still supported. */
    private String email;
    /** Email OR register number. Preferred field; falls back to email if null. */
    private String identifier;
    private String password;
}
