// RegisterRequest.java
package com.mgr.campusbridge.dto.request;
import lombok.Data;
@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String department;
    private String batch;
}