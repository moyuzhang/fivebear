package com.fivebear.common.result;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统一返回结果类
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR = 500;

    /**
     * 状态码
     */
    private int code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private transient T data;

    /**
     * 时间戳
     */
    private final long timestamp = System.currentTimeMillis();

    public Result() {}

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Manual getters to resolve "never read" warnings
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 成功返回结果
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> ok() 
    {
        return new Result<>(SUCCESS, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param <T>  数据类型
     * @return Result
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, "操作成功", data);
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(SUCCESS, message, data);
    }

    /**
     * 成功返回结果
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> ok(String message) {
        return new Result<>(SUCCESS, message);
    }

    /**
     * 失败返回结果
     *
     * @param <T> 数据类型
     * @return Result
     */
    public static <T> Result<T> fail() {
        return new Result<>(ERROR, "操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ERROR, message);
    }

    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 错误信息
     * @param data    数据
     * @param <T>     数据类型
     * @return Result
     */
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 是否成功
     *
     * @return boolean
     */
    public boolean isSuccess() {
        return this.code == SUCCESS;
    }

    /**
     * 是否失败
     *
     * @return boolean
     */
    public boolean isError() {
        return !isSuccess();
    }
} 