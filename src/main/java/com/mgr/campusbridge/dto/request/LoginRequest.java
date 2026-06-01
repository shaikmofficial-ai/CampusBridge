// LoginRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
@Data
public class LoginRequest {
    private String email;
    private String password;
}