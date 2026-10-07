package com.xiaoyan.aiassistant.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

// 收藏业务逻辑。
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public List<Favorite> list(String userId, String type) {
        if (StringUtils.hasText(type)) {
            return favoriteMapper.listByUserAndType(userId, type);
        }
        return favoriteMapper.listByUser(userId);
    }

    public Favorite add(FavoriteRequest request) {
        if (!StringUtils.hasText(request.userId())) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (!StringUtils.hasText(request.type())) {
            throw new IllegalArgumentException("type 不能为空");
        }
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("content 不能为空");
        }
        Favorite f = new Favorite();
        f.setUserId(request.userId());
        f.setType(request.type());
        f.setTitle(StringUtils.hasText(request.title()) ? request.title() : "未命名");
        f.setContent(request.content());
        f.setTags(request.tags());
        f.setSourceType(request.sourceType());
        favoriteMapper.insert(f);
        return f;
    }

    public Favorite update(Long id, FavoriteRequest request) {
        Favorite f = favoriteMapper.findById(id);
        if (f == null) {
            throw new IllegalArgumentException("收藏不存在");
        }
        if (!f.getUserId().equals(request.userId())) {
            throw new IllegalArgumentException("无权修改");
        }
        f.setTitle(request.title());
        f.setTags(request.tags());
        favoriteMapper.update(f);
        return favoriteMapper.findById(id);
    }

    public void delete(Long id, String userId) {
        favoriteMapper.delete(id, userId);
    }
}
