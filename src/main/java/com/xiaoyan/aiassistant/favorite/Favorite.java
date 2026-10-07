package com.xiaoyan.aiassistant.favorite;

import lombok.Data;

import java.time.LocalDateTime;

// 收藏实体。
@Data
public class Favorite {
    private Long id;
    private String userId;
    private String type;        // 'image' | 'chat'
    private String title;
    private String content;     // image: base64; chat: JSON 字符串
    private String tags;
    private String sourceType;  // 'upload' | 'draw' | 'chat'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
