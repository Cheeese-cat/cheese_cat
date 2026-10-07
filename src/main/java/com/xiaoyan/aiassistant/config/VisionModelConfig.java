package com.xiaoyan.aiassistant.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

// 单独装配硅基流动的视觉模型，用于图片评价。
@Configuration
public class VisionModelConfig {

    @Value("${langchain4j.open-ai.vision-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.vision-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.vision-model.model-name}")
    private String modelName;

    @Bean(name = "visionModel")
    public ChatModel visionModel() {
        return OpenAiChatModel.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey)
            .modelName(modelName)
            .timeout(Duration.ofSeconds(120))
            .logRequests(true)
            .logResponses(true)
            .build();
    }
}
