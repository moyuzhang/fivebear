package com.fivebear;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * FiveBear企业管理系统启动类
 * 
 * @author FiveBear
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.fivebear.**.mapper")
public class FiveBearSystemApplication {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FiveBearSystemApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FiveBearSystemApplication.class, args);
        log.info("\n=================================");
        log.info("    FiveBear企业管理系统启动成功!");
        log.info("    API文档地址: http://localhost:8080/doc.html");
        log.info("=================================\n");
    }
}