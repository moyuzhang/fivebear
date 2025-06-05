package com.fivebear.admin.controller;

import com.fivebear.admin.entity.Admin;
import com.fivebear.admin.service.AdminService;
import com.fivebear.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理员管理控制器
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Tag(name = "管理员管理", description = "管理员用户相关接口")
@RestController
@RequestMapping("/admin")
@Validated
public class AdminController {

    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "获取管理员列表", description = "分页查询管理员用户列表")
    @GetMapping("/list")
    public Result<List<Admin>> getAdminList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        
        List<Admin> admins = adminService.getAdminList(page, size, keyword);
        return Result.ok(admins);
    }

    @Operation(summary = "获取管理员详情", description = "根据ID获取管理员详细信息")
    @GetMapping("/{id}")
    public Result<Admin> getAdminById(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id) {
        
        Admin admin = adminService.getAdminById(id);
        return Result.ok(admin);
    }

    @Operation(summary = "新增管理员", description = "创建新的管理员用户")
    @PostMapping
    public Result<Admin> createAdmin(@Valid @RequestBody Admin admin) {
        Admin createdAdmin = adminService.createAdmin(admin);
        return Result.ok(createdAdmin, "管理员创建成功");
    }

    @Operation(summary = "更新管理员", description = "更新管理员用户信息")
    @PutMapping("/{id}")
    public Result<Admin> updateAdmin(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody Admin admin) {
        
        admin.setId(id);
        Admin updatedAdmin = adminService.updateAdmin(admin);
        return Result.ok(updatedAdmin, "管理员更新成功");
    }

    @Operation(summary = "删除管理员", description = "删除指定的管理员用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdmin(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id) {
        
        adminService.deleteAdmin(id);
        return Result.ok("管理员删除成功");
    }

    @Operation(summary = "重置管理员密码", description = "重置管理员登录密码")
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        
        adminService.resetPassword(id, newPassword);
        return Result.ok("密码重置成功");
    }

    @Operation(summary = "启用/禁用管理员", description = "启用或禁用管理员账户")
    @PostMapping("/{id}/toggle-status")
    public Result<Void> toggleAdminStatus(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id) {
        
        adminService.toggleAdminStatus(id);
        return Result.ok("状态修改成功");
    }

    @Operation(summary = "获取管理员权限", description = "获取管理员的权限列表")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getAdminPermissions(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id) {
        
        List<String> permissions = adminService.getAdminPermissions(id);
        return Result.ok(permissions);
    }

    @Operation(summary = "分配权限", description = "为管理员分配权限")
    @PostMapping("/{id}/assign-permissions")
    public Result<Void> assignPermissions(
            @Parameter(description = "管理员ID", example = "1") @PathVariable Long id,
            @RequestBody List<String> permissions) {
        
        adminService.assignPermissions(id, permissions);
        return Result.ok("权限分配成功");
    }
} 