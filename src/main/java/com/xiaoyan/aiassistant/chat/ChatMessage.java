package com.xiaoyan.aiassistant.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 单条对话消息。
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private String userId;
    private String sessionId;
    private String role;      // "user" / "assistant"
    private String content;
    private String image;     // base64，可为 null
    private LocalDateTime createdAt;
}
