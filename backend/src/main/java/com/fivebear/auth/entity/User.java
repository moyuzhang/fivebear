package com.fivebear.auth.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("username")
    private String username;
    
    @TableField("password")
    private String password;
    
    @TableField("email")
    private String email;
    
    @TableField("status")
    private Integer status; // 1-启用 0-禁用
    
    @TableField("role_id")
    private Long roleId;
    
    @TableField("nickname")
    private String nickname;
    
    @TableField("last_login_ip")
    private String lastLoginIp;
    
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
    
    @TableField("need_change_password")
    private Boolean needChangePassword;
    
    @TableField("login_failure_count")
    private Integer loginFailureCount;
    
    @TableField("account_locked_time")
    private LocalDateTime accountLockedTime;
    
    @TableField("max_concurrent_sessions")
    private Integer maxConcurrentSessions;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
    
    @TableField("create_by")
    private Long createBy;
    
    @TableField("update_by")
    private Long updateBy;
    
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
    
    @TableField("deleted_by")
    private Long deletedBy;
    
    @TableField("deleted_time")
    private LocalDateTime deletedTime;
}