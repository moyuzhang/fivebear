package com.fivebear.fivebear_system.common;

import lombok.Data;

@Data
public class ResultView<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private int code;

    public static <T> ResultView<T> success(T data) {
        ResultView<T> result = new ResultView<>();
        result.setSuccess(true);
        result.setMessage("操作成功");
        result.setData(data);
        result.setCode(200);
        return result;
    }

    public static <T> ResultView<T> success(String message, T data) {
        ResultView<T> result = new ResultView<>();
        result.setSuccess(true);
        result.setMessage(message);
        result.setData(data);
        result.setCode(200);
        return result;
    }

    public static <T> ResultView<T> fail(String message) {
        ResultView<T> result = new ResultView<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setCode(400);
        return result;
    }

    public static <T> ResultView<T> fail(String message, String error) {
        ResultView<T> result = new ResultView<>();
        result.setSuccess(false);
        result.setMessage(message);
        result.setError(error);
        result.setCode(400);
        return result;
    }
} 