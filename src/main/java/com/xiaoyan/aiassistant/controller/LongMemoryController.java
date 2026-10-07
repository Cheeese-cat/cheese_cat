package com.xiaoyan.aiassistant.controller;

import com.xiaoyan.aiassistant.memory.LongMemory;
import com.xiaoyan.aiassistant.memory.LongMemoryRequest;
import com.xiaoyan.aiassistant.memory.LongMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 长期记忆的 HTTP 接口。
@RestController
@RequestMapping("/api/memories")
@RequiredArgsConstructor
public class LongMemoryController {

    private final LongMemoryService longMemoryService;

    // 查询某个用户的长期记忆列表。
    @GetMapping
    public List<LongMemory> list(@RequestParam String userId) {
        return longMemoryService.list(userId);
    }

    // 新增一条长期记忆。
    @PostMapping
    public LongMemory add(@RequestBody LongMemoryRequest request) {
        return longMemoryService.add(request);
    }

    // 修改一条长期记忆。
    @PutMapping("/{id}")
    public LongMemory update(@PathVariable Long id, @RequestBody LongMemoryRequest request) {
        return longMemoryService.update(id, request);
    }

    // 删除一条长期记忆。
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        longMemoryService.delete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已删除");
        return result;
    }
}
