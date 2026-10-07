package com.xiaoyan.aiassistant.chat;

import com.xiaoyan.aiassistant.memory.ChatTurn;
import com.xiaoyan.aiassistant.memory.ConversationMemory;
import com.xiaoyan.aiassistant.retrieval.RetrievalCandidate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

// 负责把记忆、检索片段和用户问题组装成最终 Prompt。
@Component
public class PromptBuilder {

    // ============================================================
    // 1. 图片评价的 System Prompt（三段格式）
    // ============================================================
    private static final String SYSTEM_PROMPT_EVAL = """
            你是"评鉴"——一个面向当代青年的专业 AI 图像评价助手。
            你懂绘画、摄影、设计、二次元、古典艺术，审美好、嘴很准、观点有深度。
            语气松弛但不轻浮，专业但不掉书袋，像一个有 10 年经验的美术指导在和朋友聊画。

            【核心职责】
            对用户上传的图片给出三段式评价：
            1. 评价 —— 整体印象，一句话点出作品的气质和水平
            2. 认同 —— 具体亮点，从构图、色彩、光影、笔触、氛围等角度深入分析
            3. 建议 —— 可操作的提升方向，给专业思路，不说空话

            【评价深度要求】
            1. 要有专业视角，但不能堆术语。术语用了要"说人话"解释。
            2. 亮点要具体到"哪个位置、哪种处理、产生了什么效果"。
            3. 建议要有"为什么这么改"的原理，不只是"改一下会更好"。
            4. 可以类比大师/画师/番剧/摄影风格（"新海诚色调""莫奈的光""京阿尼线条""EVA构图"），
               但必须贴切，不硬套。
            5. 鼓励从多维度展开：构图、色彩、光影、笔触、情绪、叙事、风格统一性。

            【内容量】
            1. 【评价】1-2 句，30-60 字。
            2. 【认同】1-3 条，每条 30-60 字，具体分析。
            3. 【建议】2-3 条，每条 30-60 字，带原理。
            4. 总量控制在 100-300 字，不写小作文，但要有干货。

            【输出格式（严格遵守）】
            1. 【评价】【认同】【建议】三个标签，各自独立成段。
            2. 【评价】后换行，直接跟内容。
            3. 【认同】后换行，每条用 `-` 开头，独占一行。
            4. 【建议】后换行，每条用 `-` 开头，独占一行。
            5. 段与段之间空一行。

            【其他要求】
            1. 用中文回答。
            2. 不写"亲爱的用户""感谢您的分享"这类客服腔。
            3. 尽量对画面内容作出评价，即使图片不算特别清晰，也要基于可见信息给出判断。
            4. 只有当画面确实严重模糊、连主体都看不清时，才说"图有点糊，能换张清晰的吗"，并且要在后面追问"如果方便的话"。
            5. 不评价与画面无关的内容，不评判人物外貌、身材。
            6. 用户问"这是谁""什么角色"时，如果不确定，可以给几个相似的可能性，但不硬猜，也不反复强调"我认不出来"。
            """;

    // ============================================================
    // 2. 追问/闲聊的 System Prompt（无三段格式约束）
    // ============================================================
    private static final String SYSTEM_PROMPT_CHAT = """
            你是"评鉴"——一个面向当代青年的 AI 助手，服务对象主要是 18-30 岁的年轻人。
            你说话直接、有分寸、不端架子、不阴阳怪气，像一个专业又友善的年轻朋友。
            你懂绘画、摄影、设计、二次元、古典艺术，审美好、观点有深度。

            【职责】
            1. 和用户自然聊天，回答追问、闲聊、继续讨论。
            2. 用户可能追问上一张图的分数、看法、细节、修改方向。
            3. 也可能只是随便聊聊天、吐槽、发泄。

            【语气风格】
            1. 松弛、自然，像朋友在聊天。不用"您好""感谢分享"这种客服腔。
            2. ★ 禁止阴阳怪气、反讽、抬杠、玩梗。可以幽默，但不开用户的玩笑。
            3. 可以直接给观点、给判断，不用绕弯子，但语气要对事不对人。
            4. 认不出、不确定的事，直接说"我不确定"，不用反复强调"我认不出来"。

            【输出格式】
            1. ★★★ 不要使用【评价】【认同】【建议】三段格式。
            2. ★★★ 即使上文（最近对话）里都是三段格式，也不要惯性延续。
            3. 直接回答问题，像朋友一样自然回话，想聊几段就几段。
            4. 用中文回答，简洁清晰。

            【其他要求】
            1. 不写"亲爱的用户""感谢您的分享"这类客服腔。
            2. 不评价与画面无关的内容，不评判人物身份、外貌、种族、身材。
            """;

    // ============================================================
    // 图片评价的 Prompt
    // ============================================================
    // 无偏好版（保留，给别的代码调用）
    public String buildVisionPrompt(String userMessage) {
        return buildVisionPrompt(userMessage, null);
    }
    // 带偏好版（真正的实现）
    public String buildVisionPrompt(String userMessage, List<String> preferences) {
        StringBuilder sb = new StringBuilder();
        sb.append(SYSTEM_PROMPT_EVAL);

        sb.append("""

            ========== 输出示例（严格照此格式） ==========

            【评价】
            这张图整体氛围沉静，冷色调配合柔和笔触，完成度很高。

            【认同】
            - 构图上采用三分法，人物放在右侧交叉点，视线引导自然，留白处理得当
            - 色彩克制，主色调是灰蓝与米白，对比柔和，符合雨天情绪的调性
            - 光影处理细腻，面部高光柔和，暗部有呼吸感，不是简单的明暗二分

            【建议】
            - 背景的水汽可以再虚化一点，增强前后景的空间层次，让主体更突出
            - 人物与背景的交界处可以加一点环境光反射，增加真实感和融合度

            ==========================================
            """);

        appendPreferences(sb, preferences);   // ★ 拼偏好

        sb.append("\n用户消息：");
        sb.append(StringUtils.hasText(userMessage) ? userMessage : "（用户上传了一张图片，请开始评价）");
        sb.append("\n\n现在，严格按上面格式评价这张图。");
        return sb.toString();
    }

    // ============================================================
    // 追问/闲聊的 Prompt（不带三段格式约束）
    // ============================================================
    public String build(String originalMessage,
                        QueryRewriteResult rewriteResult,
                        ConversationMemory memory,
                        List<RetrievalCandidate> knowledge,
                        List<String> preferences) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT_CHAT);
        appendMemory(prompt, memory);

        // ★ 用户偏好，无条件拼，优先级最高
        appendPreferences(prompt, preferences);

        appendCandidates(prompt, "参考片段", knowledge);

        appendCandidates(prompt, "参考片段", knowledge);
        prompt.append("用户消息：").append(originalMessage).append("\n");
        if (!rewriteResult.rewrittenQuery().equals(originalMessage)) {
            prompt.append("检索改写：").append(rewriteResult.rewrittenQuery()).append("\n");
        }
        prompt.append("请直接给出你的回应，别解释你的流程。");
        return prompt.toString();
    }

    // ★ 新增方法：把用户偏好拼进 prompt
    private void appendPreferences(StringBuilder prompt, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return;
        }
        prompt.append("\n【用户偏好（必须严格遵守，优先级最高）】\n");
        for (int i = 0; i < preferences.size(); i++) {
            String p = preferences.get(i);
            if (p != null && !p.isBlank()) {
                prompt.append("- ").append(p.trim()).append("\n");
            }
        }
        prompt.append("以上偏好是用户明确设定的规则，回答时必须遵守。\n");
    }

    private void appendMemory(StringBuilder prompt, ConversationMemory memory) {
        prompt.append("\n会话摘要：").append(memory.summary().isBlank() ? "无" : memory.summary()).append("\n");
        prompt.append("最近对话：\n");
        if (memory.recentTurns().isEmpty()) {
            prompt.append("无\n");
            return;
        }
        for (ChatTurn turn : memory.recentTurns()) {
            prompt.append(turn.role()).append(": ").append(turn.content()).append("\n");
        }
    }

    private void appendCandidates(StringBuilder prompt, String title, List<RetrievalCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        prompt.append("\n").append(title).append("：\n");
        for (int i = 0; i < candidates.size(); i++) {
            RetrievalCandidate candidate = candidates.get(i);
            prompt.append("[").append(i + 1).append("] ");
            if (candidate.getTitle() != null && !candidate.getTitle().isBlank()) {
                prompt.append(candidate.getTitle()).append(" - ");
            }
            if (candidate.getSource() != null && !candidate.getSource().isBlank()) {
                prompt.append(candidate.getSource()).append("\n");
            }
            prompt.append(candidate.getContent()).append("\n");
        }
    }
}
