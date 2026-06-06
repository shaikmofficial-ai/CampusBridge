// AuthResponse.java
package com.mgr.campusbridge.dto.response;
import lombok.*;
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private String role;
    private String registerNumber;
}
