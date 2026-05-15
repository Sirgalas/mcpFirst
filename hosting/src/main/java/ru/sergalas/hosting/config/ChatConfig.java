package ru.sergalas.hosting.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean(name = "ollama")
    public ChatClient chatClientOllama(@Qualifier("ollamaChatModel") ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OllamaChatOptions.builder()
                    .temperature(0.1)
                    .topK(10)
                    .topP(0.95)
                    .repeatPenalty(1.0))
                .build();
    }
    @Bean(name = "google")
    public ChatClient chatClientGoogle(@Qualifier("googleGenAiChatModel") ChatModel chatModel, ToolCallbackProvider toolCallbackProvider) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultOptions(OllamaChatOptions.builder()
                    .temperature(0.1)
                    .topK(10)
                    .topP(0.95)
                    .repeatPenalty(1.0))
                .build();
    }
}
