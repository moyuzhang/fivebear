package com.fivebear.fivebear_system.dto;

import com.fivebear.fivebear_system.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private User userInfo;
    private String token;
} 