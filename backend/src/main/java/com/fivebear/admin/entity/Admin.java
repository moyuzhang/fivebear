package com.fivebear.admin.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 管理员实体类
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Data
@TableName("sys_admin")
public class Admin {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态：1=启用，0=禁用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 角色列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<String> roles;

    /**
     * 权限列表（不映射到数据库）
     */
    @TableField(exist = false)
    private List<String> permissions;

    // 手动添加链式调用的setter方法
    public Admin setId(Long id) {
        this.id = id;
        return this;
    }

    public Admin setUsername(String username) {
        this.username = username;
        return this;
    }

    public Admin setPassword(String password) {
        this.password = password;
        return this;
    }

    public Admin setRealName(String realName) {
        this.realName = realName;
        return this;
    }

    public Admin setEmail(String email) {
        this.email = email;
        return this;
    }

    public Admin setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Admin setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Admin setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Admin setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }

    public Admin setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Admin setDeleted(Integer deleted) {
        this.deleted = deleted;
        return this;
    }

    public Admin setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public Admin setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    // 手动添加getter方法
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }
} 