package com.xiaoyan.aiassistant.favorite;

// 收藏的请求体。
public record FavoriteRequest(
    String userId,
    String type,
    String title,
    String content,
    String tags,
    String sourceType
) {}
