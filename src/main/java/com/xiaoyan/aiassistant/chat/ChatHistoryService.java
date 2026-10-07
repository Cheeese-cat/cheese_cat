package com.xiaoyan.aiassistant.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

// 会话历史记录服务。
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryMapper mapper;

    // 保存一条消息；同时确保会话存在 + 更新时间
    public void saveMessage(String userId, String sessionId, String role,
                            String content, String image) {
        LocalDateTime now = LocalDateTime.now();

        ChatSession session = mapper.findSession(userId, sessionId);
        if (session == null) {
            String title = StringUtils.hasText(content)
                ? content.substring(0, Math.min(30, content.length()))
                : "新对话";
            mapper.insertSession(new ChatSession(null, userId, sessionId, title, now, now));
        } else {
            mapper.touchSession(userId, sessionId, now);
        }

        mapper.insertMessage(new ChatMessage(null, userId, sessionId, role, content, image, now));
    }

    // 列出用户所有会话
    public List<ChatSession> listSessions(String userId) {
        return mapper.listSessions(userId);
    }

    // 某个会话的所有消息
    public List<ChatMessage> listMessages(String userId, String sessionId) {
        return mapper.listMessages(userId, sessionId);
    }

    // 删除整个会话
    public void deleteSession(String userId, String sessionId) {
        mapper.deleteMessagesBySession(userId, sessionId);
        mapper.deleteSession(userId, sessionId);
    }

    // 修改某条用户消息，并删除其后所有消息（用于"编辑后重问"）
    public void updateUserMessage(Long id, String newContent) {
        ChatMessage msg = mapper.findMessage(id);
        if (msg == null || !"user".equals(msg.getRole())) return;

        // 删掉这条之后的所有消息（包括这条），重新插入新的
        mapper.deleteMessagesFrom(msg.getUserId(), msg.getSessionId(), id);
        saveMessage(msg.getUserId(), msg.getSessionId(), "user", newContent, msg.getImage());
    }

    // 删除某条消息及其后所有（用于"重新回答"）
    public void deleteFrom(Long id) {
        ChatMessage msg = mapper.findMessage(id);
        if (msg == null) return;
        mapper.deleteMessagesFrom(msg.getUserId(), msg.getSessionId(), id);
    }

    // 更新会话标题
    public void renameSession(String userId, String sessionId, String title) {
        mapper.updateSessionTitle(userId, sessionId, title, LocalDateTime.now());
    }
}
