package com.fivebear.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fivebear.auth.service.LoginSecurityService;
import com.fivebear.common.result.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 登录安全控制器
 */
@Tag(name = "登录安全管理", description = "登录安全相关接口")
@RestController
@RequestMapping("/auth/security")
public class LoginSecurityController {
    
    @Autowired(required = false)
    private LoginSecurityService loginSecurityService;
    
    @Operation(summary = "检查账户锁定状态")
    @GetMapping("/lock-status")
    public Result<Map<String, Object>> checkLockStatus(@RequestParam(required = true) String username) {
        try {
            // 参数验证
            if (username == null || username.trim().isEmpty()) {
                return Result.fail(400, "用户名参数不能为空");
            }
            
            Map<String, Object> data = new HashMap<>();
            
            // 如果LoginSecurityService不可用，返回默认值
            if (loginSecurityService == null) {
                data.put("isLocked", false);
                data.put("remainingAttempts", 5);
                data.put("securityServiceAvailable", false);
            } else {
                boolean isLocked = loginSecurityService.isAccountLocked(username.trim());
                data.put("isLocked", isLocked);
                data.put("securityServiceAvailable", true);
                
                if (isLocked) {
                    long remainingTime = loginSecurityService.getLockTimeRemaining(username.trim());
                    data.put("remainingTime", remainingTime);
                    data.put("remainingMinutes", remainingTime / 60);
                } else {
                    int remainingAttempts = loginSecurityService.getRemainingAttempts(username.trim());
                    data.put("remainingAttempts", remainingAttempts);
                }
            }
            
            return Result.ok(data, "查询成功");
            
        } catch (Exception e) {
            return Result.fail(500, "查询账户状态失败: " + e.getMessage());
        }
    }
} 