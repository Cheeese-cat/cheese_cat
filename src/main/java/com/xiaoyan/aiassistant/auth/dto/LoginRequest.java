// src/main/java/com/xiaoyan/aiassistant/auth/dto/LoginRequest.java
package com.xiaoyan.aiassistant.auth.dto;

public class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
