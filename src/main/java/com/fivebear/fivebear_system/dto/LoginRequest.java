package com.fivebear.fivebear_system.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String account;
    private String password;
} 