package com.fivebear.fivebear_system.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private int code;

    private ApiResponse(boolean success, String message, T data, String error, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.code = code;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data, null, 200);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, 200);
    }

    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(false, null, null, error, 400);
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return new ApiResponse<>(false, message, null, error, 400);
    }

    public static <T> ApiResponse<T> unauthorized(String error) {
        return new ApiResponse<>(false, null, null, error, 401);
    }
} 