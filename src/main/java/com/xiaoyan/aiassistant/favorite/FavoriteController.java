package com.xiaoyan.aiassistant.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 收藏的 HTTP 接口。
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // 列表。type 可选：image / chat
    @GetMapping
    public List<Favorite> list(@RequestParam String userId,
                               @RequestParam(required = false) String type) {
        return favoriteService.list(userId, type);
    }

    // 新增收藏。
    @PostMapping
    public Favorite add(@RequestBody FavoriteRequest request) {
        return favoriteService.add(request);
    }

    // 修改收藏（只允许改 title / tags）。
    @PutMapping("/{id}")
    public Favorite update(@PathVariable Long id, @RequestBody FavoriteRequest request) {
        return favoriteService.update(id, request);
    }

    // 删除收藏。
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, @RequestParam String userId) {
        favoriteService.delete(id, userId);
        return Map.of("success", true);
    }
}
