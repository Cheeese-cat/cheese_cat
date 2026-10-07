package com.xiaoyan.aiassistant.chat;

import java.util.List;

// 聊天请求体，包含用户、会话、本轮问题、可选图片、用户偏好。
public record ChatRequest(
    String userId,
    String sessionId,
    String message,
    String image,
    List<String> preferences
) {
}
