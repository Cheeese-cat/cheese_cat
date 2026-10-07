package com.xiaoyan.aiassistant.controller;

import com.xiaoyan.aiassistant.chat.ChatHistoryService;
import com.xiaoyan.aiassistant.chat.ChatMessage;
import com.xiaoyan.aiassistant.chat.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 会话历史接口。
@RestController
@RequestMapping("/api/chat/history")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService historyService;

    // 列出用户所有会话
    @GetMapping("/sessions")
    public List<ChatSession> listSessions(@RequestParam String userId) {
        return historyService.listSessions(userId);
    }

    // 某个会话的消息列表
    @GetMapping("/messages")
    public List<ChatMessage> listMessages(@RequestParam String userId,
                                          @RequestParam String sessionId) {
        return historyService.listMessages(userId, sessionId);
    }

    // 删除会话
    @DeleteMapping("/session")
    public Map<String, Object> deleteSession(@RequestParam String userId,
                                             @RequestParam String sessionId) {
        historyService.deleteSession(userId, sessionId);
        return ok("会话已删除");
    }

    // 修改用户消息（之后的消息会一并删除，前端再触发重问）
    @PutMapping("/message/{id}")
    public Map<String, Object> updateMessage(@PathVariable Long id,
                                             @RequestBody Map<String, String> body) {
        historyService.updateUserMessage(id, body.getOrDefault("content", ""));
        return ok("消息已更新");
    }

    // 从某条消息起全部删除（"重新回答"用）
    @DeleteMapping("/message/{id}/from")
    public Map<String, Object> deleteFrom(@PathVariable Long id) {
        historyService.deleteFrom(id);
        return ok("已删除后续消息");
    }

    // 重命名会话
    @PutMapping("/session/rename")
    public Map<String, Object> renameSession(@RequestBody Map<String, String> body) {
        historyService.renameSession(
            body.get("userId"),
            body.get("sessionId"),
            body.get("title")
        );
        return ok("已重命名");
    }

    private Map<String, Object> ok(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", true);
        m.put("message", msg);
        return m;
    }
}
