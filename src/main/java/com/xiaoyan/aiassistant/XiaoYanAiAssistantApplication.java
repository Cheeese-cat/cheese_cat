package com.xiaoyan.aiassistant;

import com.xiaoyan.aiassistant.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 启动类。
@SpringBootApplication
public class XiaoYanAiAssistantApplication implements CommandLineRunner {

    @Autowired
    private AuthService authService;

    // Spring Boot 应用入口。
    public static void main(String[] args) {
        SpringApplication.run(XiaoYanAiAssistantApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 创建默认管理员账号（首次启动时自动创建）
        authService.createDefaultUser();
    }
}
