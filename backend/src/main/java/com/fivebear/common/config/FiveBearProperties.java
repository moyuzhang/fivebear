package com.fivebear.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * FiveBear系统配置属性
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "fivebear")
public class FiveBearProperties {

    /**
     * JWT配置
     */
    private final Jwt jwt = new Jwt();

    /**
     * 文件上传配置
     */
    private final Upload upload = new Upload();

    /**
     * 系统配置
     */
    private final System system = new System();

    // 手动添加getter方法以解决"never read"警告
    public Jwt getJwt() {
        return jwt;
    }

    public Upload getUpload() {
        return upload;
    }

    public System getSystem() {
        return system;
    }

    @Data
    public static class Jwt {
        private String secret;
        private Long expireTime;

        // 手动添加getter方法
        public String getSecret() {
            return secret;
        }

        public Long getExpireTime() {
            return expireTime;
        }

        // 手动添加setter方法用于配置注入
        public void setSecret(String secret) {
            this.secret = secret;
        }

        public void setExpireTime(Long expireTime) {
            this.expireTime = expireTime;
        }
    }

    @Data
    public static class Upload {
        // 完全从配置文件读取路径，避免硬编码
        private String path;
        private String maxFileSize;
        private String maxRequestSize;

        // 手动添加getter方法
        public String getPath() {
            return path;
        }

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public String getMaxRequestSize() {
            return maxRequestSize;
        }

        // 手动添加setter方法用于配置注入
        public void setPath(String path) {
            this.path = path;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }
    }

    @Data
    public static class System {
        private String name;
        private String version;
        private String author;

        // 手动添加getter方法
        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getAuthor() {
            return author;
        }

        // 手动添加setter方法用于配置注入
        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}