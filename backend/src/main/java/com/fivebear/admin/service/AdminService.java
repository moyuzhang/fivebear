package com.fivebear.admin.service;

import com.fivebear.admin.entity.Admin;
import java.util.List;

/**
 * 管理员服务接口
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
public interface AdminService {

    /**
     * 获取管理员列表
     */
    List<Admin> getAdminList(Integer page, Integer size, String keyword);

    /**
     * 根据ID获取管理员
     */
    Admin getAdminById(Long id);

    /**
     * 创建管理员
     */
    Admin createAdmin(Admin admin);

    /**
     * 更新管理员
     */
    Admin updateAdmin(Admin admin);

    /**
     * 删除管理员
     */
    void deleteAdmin(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 切换管理员状态
     */
    void toggleAdminStatus(Long id);

    /**
     * 获取管理员权限
     */
    List<String> getAdminPermissions(Long id);

    /**
     * 分配权限
     */
    void assignPermissions(Long id, List<String> permissions);
} 