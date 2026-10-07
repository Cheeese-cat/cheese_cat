package com.xiaoyan.aiassistant.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 长期记忆的录入、查询和向量化服务。
@Service
@RequiredArgsConstructor
public class LongMemoryService {
    private static final String DEFAULT_USER_ID = "default";

    private final LongMemoryMapper mapper;

    // 新增一条用户主动录入的长期记忆。
    @Transactional
    public LongMemory add(LongMemoryRequest request) {
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("长期记忆内容不能为空");
        }

        String userId = normalizeUserId(request.userId());
        LocalDateTime now = LocalDateTime.now();
        LongMemory memory = new LongMemory(
            null,
            "memory-" + UUID.randomUUID(),
            userId,
            request.title(),
            request.content(),
            request.tags(),
            now,
            now
        );
        mapper.insert(memory);
        return memory;
    }

    // 查询指定用户的长期记忆列表。
    public List<LongMemory> list(String userId) {
        return mapper.findByUserId(normalizeUserId(userId));
    }

    // 根据 id 查询单条长期记忆。
    public LongMemory get(Long id) {
        return mapper.findById(id);
    }

    // 修改一条长期记忆（同时更新向量库）。
    @Transactional
    public LongMemory update(Long id, LongMemoryRequest request) {
        LongMemory existing = mapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("长期记忆不存在：" + id);
        }
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("长期记忆内容不能为空");
        }

        existing.setTitle(request.title());
        existing.setContent(request.content());
        existing.setTags(request.tags());
        existing.setUpdatedAt(LocalDateTime.now());
        mapper.update(existing);
        return existing;
    }

    // 删除一条长期记忆。
    @Transactional
    public void delete(Long id) {
        LongMemory existing = mapper.findById(id);
        if (existing == null) {
            return;
        }
        mapper.deleteById(id);
    }

    // 空 userId 统一归入 default。
    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId.trim() : DEFAULT_USER_ID;
    }
}
