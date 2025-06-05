package com.fivebear.auth.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "系统监控接口", description = "系统状态和监控相关API")
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Operation(summary = "获取系统状态")
    @GetMapping("/status")
    public ResponseEntity<?> getSystemStatus() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 系统基本信息
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("javaVersion", System.getProperty("java.version"));
            systemInfo.put("osName", osBean.getName());
            systemInfo.put("osArch", osBean.getArch());
            systemInfo.put("processors", osBean.getAvailableProcessors());
            systemInfo.put("uptime", runtimeBean.getUptime());
            systemInfo.put("startTime", new Date(runtimeBean.getStartTime()));
            
            // 内存信息
            Map<String, Object> memoryInfo = new HashMap<>();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            long totalMemory = memoryBean.getHeapMemoryUsage().getCommitted();
            
            memoryInfo.put("used", usedMemory);
            memoryInfo.put("max", maxMemory);
            memoryInfo.put("total", totalMemory);
            memoryInfo.put("usedMB", usedMemory / 1024 / 1024);
            memoryInfo.put("maxMB", maxMemory / 1024 / 1024);
            memoryInfo.put("totalMB", totalMemory / 1024 / 1024);
            memoryInfo.put("usagePercent", (double) usedMemory / maxMemory * 100);
            
            // 数据库连接状态
            Map<String, Object> dbStatus = new HashMap<>();
            try {
                Connection conn = jdbcTemplate.getDataSource().getConnection();
                dbStatus.put("connected", !conn.isClosed());
                dbStatus.put("productName", conn.getMetaData().getDatabaseProductName());
                dbStatus.put("productVersion", conn.getMetaData().getDatabaseProductVersion());
                dbStatus.put("url", conn.getMetaData().getURL());
                conn.close();
            } catch (SQLException e) {
                dbStatus.put("connected", false);
                dbStatus.put("error", e.getMessage());
            }
            
            // Redis连接状态
            Map<String, Object> redisStatus = new HashMap<>();
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set("health_check", "ok");
                    String value = (String) redisTemplate.opsForValue().get("health_check");
                    redisStatus.put("connected", "ok".equals(value));
                    redisTemplate.delete("health_check");
                } catch (Exception e) {
                    redisStatus.put("connected", false);
                    redisStatus.put("error", e.getMessage());
                }
            } else {
                redisStatus.put("connected", false);
                redisStatus.put("error", "Redis template not configured");
            }
            
            data.put("systemInfo", systemInfo);
            data.put("memory", memoryInfo);
            data.put("database", dbStatus);
            data.put("redis", redisStatus);
            data.put("timestamp", System.currentTimeMillis());
            
            result.put("code", 200);
            result.put("message", "获取系统状态成功");
            result.put("data", data);
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取系统状态失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取系统统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        
        try {
            // 用户统计
            Map<String, Object> userStats = new HashMap<>();
            Integer totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            userStats.put("total", totalUsers != null ? totalUsers : 0);
            
            // 角色统计
            Map<String, Object> roleStats = new HashMap<>();
            Integer totalRoles = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
            roleStats.put("total", totalRoles != null ? totalRoles : 0);
            
            // 站点统计（如果有sites表）
            Map<String, Object> siteStats = new HashMap<>();
            try {
                Integer totalSites = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sites", Integer.class);
                siteStats.put("total", totalSites != null ? totalSites : 0);
            } catch (Exception e) {
                siteStats.put("total", 0);
                siteStats.put("note", "站点表不存在");
            }
            
            data.put("users", userStats);
            data.put("roles", roleStats);
            data.put("sites", siteStats);
            data.put("timestamp", System.currentTimeMillis());
            
            result.put("code", 200);
            result.put("message", "获取统计信息成功");
            result.put("data", data);
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取统计信息失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取最近活动日志")
    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> activities = new ArrayList<>();
        
        try {
            // 这里可以从日志表或者其他地方获取最近的活动
            // 暂时返回一些示例数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("id", 1);
            activity1.put("time", LocalDateTime.now().minusMinutes(5).format(formatter));
            activity1.put("type", "login");
            activity1.put("description", "用户登录系统");
            activity1.put("username", "admin");
            activities.add(activity1);
            
            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("id", 2);
            activity2.put("time", LocalDateTime.now().minusMinutes(15).format(formatter));
            activity2.put("type", "system");
            activity2.put("description", "系统状态检查");
            activity2.put("username", "system");
            activities.add(activity2);
            
            Map<String, Object> activity3 = new HashMap<>();
            activity3.put("id", 3);
            activity3.put("time", LocalDateTime.now().minusMinutes(30).format(formatter));
            activity3.put("type", "operation");
            activity3.put("description", "用户权限更新");
            activity3.put("username", "admin");
            activities.add(activity3);
            
            result.put("code", 200);
            result.put("message", "获取活动日志成功");
            result.put("data", activities);
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取活动日志失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "检查管理员用户状态")
    @GetMapping("/check-admin")
    public ResponseEntity<?> checkAdmin() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询管理员用户信息
            List<Map<String, Object>> admins = jdbcTemplate.queryForList(
                "SELECT id, username, nickname, email, role_id, status, deleted, created_at " +
                "FROM users WHERE role_id = 1 ORDER BY created_at"
            );
            
            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", Map.of(
                "adminCount", admins.size(),
                "admins", admins
            ));
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询管理员失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "初始化管理员用户")
    @PostMapping("/init-admin")
    public ResponseEntity<?> initAdmin() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查是否已存在管理员用户
            Integer adminCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role_id = 1 AND deleted = false", 
                Integer.class
            );
            
            if (adminCount != null && adminCount > 0) {
                result.put("code", 200);
                result.put("message", "管理员用户已存在");
                result.put("data", Map.of("adminExists", true));
                return ResponseEntity.ok(result);
            }
            
            // 创建默认管理员用户
            // 密码: admin123 (BCrypt加密)
            String encryptedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKV4o2eZKtbkyy/QGCnwKfZhp2xG";
            
            // 先确保角色表有数据
            jdbcTemplate.update(
                "INSERT IGNORE INTO roles (id, role_name, description, status, created_at, updated_at) VALUES " +
                "(1, 'ADMIN', '系统管理员', 1, NOW(), NOW()), " +
                "(2, 'SUPERVISOR', '系统总监', 1, NOW(), NOW()), " +
                "(3, 'MAJOR_SHAREHOLDER', '系统大股东', 1, NOW(), NOW())"
            );
            
            // 创建管理员用户
            int rowsAffected = jdbcTemplate.update(
                "INSERT INTO users (username, nickname, email, password, role_id, status, deleted, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                "admin", "系统管理员", "admin@fivebear.com", encryptedPassword, 1, 1, false
            );
            
            if (rowsAffected > 0) {
                result.put("code", 200);
                result.put("message", "管理员用户创建成功");
                result.put("data", Map.of(
                    "username", "admin",
                    "password", "admin123",
                    "note", "请立即修改默认密码"
                ));
            } else {
                result.put("code", 500);
                result.put("message", "管理员用户创建失败");
            }
            
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "初始化管理员失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return ResponseEntity.ok(result);
    }
} 