package com.fivebear.auth.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fivebear.auth.entity.LoginRequest;
import com.fivebear.auth.entity.LoginResponse;
import com.fivebear.auth.service.AuthService;
import com.fivebear.common.result.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "用户登录、登出、验证等接口")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return Result.ok(loginResponse, "登录成功");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
    
    @Operation(summary = "用户登出", description = "用户登出，注销令牌")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7); // 移除 "Bearer " 前缀
            authService.logout(token);
            return Result.ok("登出成功");
        } catch (Exception e) {
            return Result.fail("登出失败");
        }
    }
    
    @Operation(summary = "获取用户信息", description = "根据令牌获取当前用户信息")
    @GetMapping("/user-info")
    public Result<LoginResponse.UserInfo> getUserInfo(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7); // 移除 "Bearer " 前缀
            LoginResponse.UserInfo userInfo = authService.getUserInfoByToken(token);
            if (userInfo != null) {
                return Result.ok(userInfo);
            } else {
                return Result.fail(401, "令牌无效或已过期");
            }
        } catch (Exception e) {
            return Result.fail(401, "获取用户信息失败");
        }
    }
    
    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    @PostMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7); // 移除 "Bearer " 前缀
            boolean isValid = authService.validateToken(token);
            return Result.ok(isValid);
        } catch (Exception e) {
            return Result.ok(false);
        }
    }
} 