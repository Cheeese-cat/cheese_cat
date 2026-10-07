// src/main/java/com/xiaoyan/aiassistant/controller/AuthController.java
package com.xiaoyan.aiassistant.controller;

import com.xiaoyan.aiassistant.auth.dto.LoginRequest;
import com.xiaoyan.aiassistant.auth.dto.LoginResponse;
import com.xiaoyan.aiassistant.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword());
    }

    @GetMapping("/verify")
    public Map<String, Object> verifyToken(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        boolean valid = authService.verifyToken(token);
        result.put("valid", valid);
        if (valid) {
            result.put("username", authService.extractUsername(token));
        }
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登出成功");
        return result;
    }
}
