package com.xiaoyan.aiassistant.chat;

import com.xiaoyan.aiassistant.memory.ConversationMemory;
import com.xiaoyan.aiassistant.memory.ShortTermMemoryService;
import com.xiaoyan.aiassistant.retrieval.HybridRetrievalService;
import com.xiaoyan.aiassistant.retrieval.RetrievalCandidate;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;

// 负责在线问答主链路编排。
@Service
@RequiredArgsConstructor
public class ChatService {
    private static final String DEFAULT_USER_ID = "default";

    private final ShortTermMemoryService shortTermMemoryService;
    private final QueryRewriteService queryRewriteService;
    private final HybridRetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final StreamingChatModel streamingChatModel;
    private final VisionService visionService;
    private final ChatHistoryService chatHistoryService;

    // 在线问答主流程。
    public Flux<String> stream(ChatRequest request) {
        String userId = normalizeUserId(request.userId());
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : "default";
        String memorySessionId = userId + ":" + sessionId;
        String message = StringUtils.hasText(request.message()) ? request.message().trim() : "";
        String image = request.image();

        // ★ 只要带图片 → 走视觉模型 + 三段格式
        if (StringUtils.hasText(image)) {
            return streamWithVision(userId, sessionId, memorySessionId, message, image, request.preferences());
        }

        // 无图片：走 RAG
        return Flux.create(sink -> {
            ConversationMemory memory = shortTermMemoryService.get(memorySessionId);
            QueryRewriteResult rewrite = queryRewriteService.rewrite(message, memory);
            List<String> semanticQueries = rewrite.semanticQueries(message);
            List<RetrievalCandidate> knowledge = retrievalService.retrieveKnowledge(semanticQueries, rewrite.keywordText(message));
            String prompt = promptBuilder.build(message, rewrite, memory, knowledge, request.preferences());
            StringBuilder answer = new StringBuilder();
            streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    answer.append(partialResponse);
                    sink.next(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    try {
                        shortTermMemoryService.append(memorySessionId, message, answer.toString());
                    } catch (Exception ignored) { }
                    try {
                        chatHistoryService.saveMessage(userId, sessionId, "user", message, null);
                        chatHistoryService.saveMessage(userId, sessionId, "assistant", answer.toString(), null);
                    } catch (Exception e) {
                        System.err.println("❌ 保存消息失败: " + e.getMessage());
                    }
                    sink.complete();
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            });
        });
    }

    // 图片路径：调用视觉模型 + 三段格式
    private Flux<String> streamWithVision(String userId, String sessionId, String memorySessionId, String message, String image, List<String> preferences) {
        return Flux.create(sink -> {
            try {
                String prompt = promptBuilder.buildVisionPrompt(message, preferences);
                String result = visionService.evaluate(image, prompt);
                String formatted = lightFormat(result);

                sink.next(formatted);

                String userMsg = StringUtils.hasText(message) ? message : "（上传了一张图片）";
                try {
                    shortTermMemoryService.append(memorySessionId, userMsg, formatted);
                } catch (Exception ignored) { }

                try {
                    chatHistoryService.saveMessage(userId, sessionId, "user", userMsg, image);
                    chatHistoryService.saveMessage(userId, sessionId, "assistant", formatted, null);
                } catch (Exception e) {
                    System.err.println("❌ 保存消息失败: " + e.getMessage());
                }

                sink.complete();
            } catch (Exception e) {
                sink.next("【请求失败】" + e.getClass().getSimpleName() + "：" + e.getMessage());
                sink.complete();
            }
        });
    }

    /**
     * 强制格式化视觉模型的输出：
     * 1. 【标签】独占一行
     * 2. 段与段之间空一行
     * 3. 清理行首缩进（含全角空格）
     * 4. 去掉 markdown 代码块
     */
    private String lightFormat(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String s = raw;

        // 0. 去掉 markdown 代码块标记
        s = s.replaceAll("```[a-zA-Z]*\\n?", "");
        s = s.replaceAll("```", "");

        // 1. 统一已有 - 的格式
        s = s.replaceAll("(?m)^[ \\t\\u3000]*-[ \\t\\u3000]*", "- ");

        // 2. 所有【标签】后换行（不空行）
        s = s.replaceAll("【评价】[ \\t\\n]*", "【评价】\n");
        s = s.replaceAll("【认同】[ \\t\\n]*", "【认同】\n");
        s = s.replaceAll("【建议】[ \\t\\n]*", "【建议】\n");

        // 3. 【标签】前面确保空一行
        s = s.replaceAll("([^\\n])【", "$1\n\n【");

        // 4. 去掉开头多余换行
        s = s.replaceAll("^\\n+", "");

        // 5. 压缩 3+ 空行成 2 个
        s = s.replaceAll("\\n{3,}", "\n\n");

        // 6. 行首行尾空格清理（保留 - 开头的行）
        s = s.replaceAll("[ \\t\\u3000]+\\n", "\n");
        s = s.replaceAll("(?m)^(?!-)[ \\t\\u3000]+", "");

        // ★ 7. 【认同】【建议】下每行自动补 -
        s = addDashToList(s, "【认同】");
        s = addDashToList(s, "【建议】");

        // ★ 8. 修复"一两个字独占一行"：
        //    如果某行不以标签/`- `开头，且长度很短（<=4字），把它合并到上一行
        s = mergeShortLines(s);

        // ★ 9. 给【认同】【建议】下的列表项末尾补句号
        s = addPeriodToList(s);

        return s.trim();
    }

    /**
     * 给指定标签下的内容，每一行自动加 "- " 前缀。
     * 已经有 "- " 的不重复加。
     */
    private String addDashToList(String text, String tag) {
        int tagIdx = text.indexOf(tag);
        if (tagIdx < 0) return text;

        // 找到标签后的内容起点
        int start = tagIdx + tag.length();

        // 找到下一个【标签】或结尾
        int nextTag = findNextTag(text, start);

        // 取出标签后的内容段
        String section = text.substring(start, nextTag);

        // 逐行处理
        String[] lines = section.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                sb.append(line).append("\n");
            } else if (trimmed.startsWith("- ")) {
                sb.append(trimmed).append("\n");
            } else {
                // ★ 自动补 "- "
                sb.append("- ").append(trimmed).append("\n");
            }
        }

        return text.substring(0, start) + sb + text.substring(nextTag);
    }

    /**
     * 找下一个【标签】的位置，找不到返回 text.length()。
     */
    private int findNextTag(String text, int from) {
        int next = text.length();
        for (String tag : new String[]{"【评价】", "【认同】", "【建议】"}) {
            int idx = text.indexOf(tag, from);
            if (idx >= 0 && idx < next) {
                next = idx;
            }
        }
        return next;
    }

    /**
     * 合并"被硬换行切碎的句子"。
     * 规则：如果上一行末尾没有标点，且本行以标点开头，则合并。
     */
    private String mergeShortLines(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                sb.append("\n");
                continue;
            }

            // 取出"上一行已经写入的内容"（去掉末尾 \n）
            String prev = sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n'
                ? sb.substring(0, sb.length() - 1)
                : sb.toString();

            // 上一行最后一个字符
            char prevLast = prev.isEmpty() ? '\0' : prev.charAt(prev.length() - 1);
            // 本行第一个字符
            char curFirst = trimmed.charAt(0);

            // 判断：
            // - 上一行末尾不是标点
            // - 本行以标点开头（逗号/句号/问号等）
            // - 本行不是标签行、不是 - 开头
            boolean prevNoPunct = "。！？.!?、，；：\"\"''".indexOf(prevLast) < 0;
            boolean curStartsWithPunct = "。！？.!?、，；：".indexOf(curFirst) >= 0;
            boolean isListLine = trimmed.startsWith("【") || trimmed.startsWith("- ");

            if (prevNoPunct && curStartsWithPunct && !isListLine && prev.length() > 0) {
                // 合并：去掉上一行的 \n，拼上本行
                sb.deleteCharAt(sb.length() - 1);
                sb.append(trimmed);
            } else {
                sb.append(trimmed).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 给【认同】【建议】下每一行末尾补句号（如果没有）。
     */
    private String addPeriodToList(String text) {
        int yiIdx = text.indexOf("【认同】");
        int jianIdx = text.indexOf("【建议】");
        int start = -1;
        if (yiIdx >= 0) start = yiIdx;
        if (jianIdx >= 0 && (start < 0 || jianIdx < start)) start = jianIdx;
        if (start < 0) return text;

        String head = text.substring(0, start);
        String body = text.substring(start);

        String[] lines = body.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        boolean inList = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.equals("【认同】") || trimmed.equals("【建议】")) {
                inList = true;
                sb.append(line).append("\n");
            } else if (trimmed.startsWith("【")) {
                inList = false;
                sb.append(line).append("\n");
            } else if (inList && !trimmed.isEmpty()) {
                // ★ 不依赖 - 前缀，任何非空行都补句号
                char last = trimmed.charAt(trimmed.length() - 1);
                if ("。！？.!?".indexOf(last) >= 0) {
                    sb.append(line).append("\n");
                } else {
                    sb.append(line).append("。").append("\n");
                }
            } else {
                sb.append(line).append("\n");
            }
        }
        return head + sb.toString().stripTrailing() + "\n";
    }

    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId.trim() : DEFAULT_USER_ID;
    }
}
