package com.xiaoyan.aiassistant.chat;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

// 视觉模型服务：把图片 + 提示词交给多模态模型，得到纯文本评价。
@Service
public class VisionService {

    private final ChatModel visionModel;

    public VisionService(@Qualifier("visionModel") ChatModel visionModel) {
        this.visionModel = visionModel;
    }

    // 让视觉模型"看图说话"，返回纯文本评价。
    public String evaluate(String base64Image, String prompt) {
        if (!StringUtils.hasText(base64Image)) {
            throw new IllegalArgumentException("图片为空");
        }

        // 去掉 data:image/png;base64, 前缀（如果前端传了完整 dataURL）
        String pureBase64 = base64Image;
        String mimeType = "image/png";
        if (base64Image.startsWith("data:")) {
            int commaIdx = base64Image.indexOf(',');
            if (commaIdx > 0) {
                String header = base64Image.substring(5, commaIdx);
                int semiIdx = header.indexOf(';');
                if (semiIdx > 0) {
                    mimeType = header.substring(0, semiIdx);
                }
                pureBase64 = base64Image.substring(commaIdx + 1);
            }
        }

        UserMessage userMessage = UserMessage.from(
            TextContent.from(prompt),
            ImageContent.from(pureBase64, mimeType)
        );

        ChatResponse response = visionModel.chat(userMessage);
        return response.aiMessage().text();
    }
}
