// src/main/java/com/xiaoyan/aiassistant/config/AuthInterceptor.java
package com.xiaoyan.aiassistant.config;

import com.xiaoyan.aiassistant.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    private static final String[] EXCLUDE_PATHS = {
        "/api/auth/login",
        "/api/auth/verify",
        "/login.html",
        "/index.html",
        "/static/**",
        "/css/**",
        "/js/**",
        "/images/**",
        "/favicon.ico"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        for (String excludePath : EXCLUDE_PATHS) {
            if (path.matches(excludePath.replace("**", ".*"))) {
                return true;
            }
        }

        if (path.startsWith("/api/")) {
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"未登录或 token 已过期\"}");
                return false;
            }

            boolean valid = authService.verifyToken(token);
            if (!valid) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"token 无效或已过期\"}");
                return false;
            }
        }

        return true;
    }
}
