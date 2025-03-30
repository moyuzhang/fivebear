package com.fivebear.fivebear_system.controller;

import com.fivebear.fivebear_system.common.ApiResponse;
import com.fivebear.fivebear_system.common.ResultView;
import com.fivebear.fivebear_system.dto.LoginRequest;
import com.fivebear.fivebear_system.dto.LoginResponse;
import com.fivebear.fivebear_system.entity.User;
import com.fivebear.fivebear_system.security.JwtUtils;
import com.fivebear.fivebear_system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody User user) {
        try {
            User registeredUser = userService.register(user);
            return ResponseEntity.ok(ApiResponse.success("注册成功", registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResultView<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 使用 Spring Security 进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getAccount(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 从数据库获取完整的用户信息
            User user = userService.findByAccount(request.getAccount());
            if (user == null) {
                return ResultView.fail("用户不存在");
            }

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            user.setPasswordChanged(false);
            userService.update(user);

            // 生成 token
            String token = jwtUtils.generateToken(user);

            // 构建返回数据，去除敏感信息
            User safeUser = new User();
            safeUser.setId(user.getId());
            safeUser.setAccount(user.getAccount());
            safeUser.setUsername(user.getUsername());
            safeUser.setFullName(user.getFullName());
            safeUser.setEmail(user.getEmail());
            safeUser.setLevel(user.getLevel());
            safeUser.setRole(user.getRole());
            safeUser.setLastLoginTime(user.getLastLoginTime());
            safeUser.setIsActive(user.getIsActive() != null ? user.getIsActive() : true);
            safeUser.setCreateTime(user.getCreateTime());
            safeUser.setParentId(user.getParentId());
            safeUser.setAuthorities(user.getAuthorities());

            LoginResponse response = new LoginResponse();
            response.setUserInfo(safeUser);
            response.setToken(token);

            return ResultView.success(response);
        } catch (Exception e) {
            e.printStackTrace(); // 添加这行来打印详细错误信息
            return ResultView.fail("登录失败：" + e.getMessage());
        }
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String account = authentication.getName();
            User user = userService.findByAccount(account);
            if (user != null) {
                return ResponseEntity.ok(ApiResponse.success(user));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    @GetMapping("/{account}")
    public ResponseEntity<ApiResponse<?>> getUserByAccount(@PathVariable String account) {
        User user = userService.findByAccount(account);
        if (user != null) {
            return ResponseEntity.ok(ApiResponse.success(user));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.update(user);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
} 