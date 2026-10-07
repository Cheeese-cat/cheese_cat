package com.xiaoyan.aiassistant.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 会话元信息。
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    private Long id;
    private String userId;
    private String sessionId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
