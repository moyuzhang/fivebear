package com.fivebear.fivebear_system.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    
    // 用户相关错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "用户已禁用"),
    USER_NOT_EXIST(1005, "用户不存在"),
    
    // 业务相关错误码
    INVALID_PARAMETER(2001, "无效的参数"),
    OPERATION_FAILED(2002, "操作失败"),
    DATA_NOT_FOUND(2003, "数据不存在"),
    VALIDATE_FAILED(2004, "参数验证失败"),
    
    // 系统相关错误码
    SYSTEM_ERROR(3001, "系统错误"),
    SERVICE_UNAVAILABLE(3002, "服务不可用");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
} 