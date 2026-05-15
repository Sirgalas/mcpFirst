package ru.sergalas.hosting;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class Host {

    @Qualifier("ollamaChatModel")
    @Autowired
    ChatModel chatModelOllama;

    @Qualifier("googleGenAiChatModel")
    @Autowired
    ChatModel chatModelGoogle;

    @Qualifier("ollama")
    @Autowired
    ChatClient chatClientOllama;

    @Qualifier("google")
    @Autowired
    ChatClient chatClientGoogle;

    @McpLogging(clients = "mcpulsor-server")
    public void doLoggerClient (McpSchema.LoggingMessageNotification loggingMessageNotification) {
        System.out.println(loggingMessageNotification.data());
    }

    @McpSampling(clients = "mcpulsor-server")
    public McpSchema.CreateMessageResult doSampling(McpSchema.CreateMessageRequest createMessageRequest) {
        ChatClient samplingClient = ChatClient.builder(chatModelGoogle)// chatClientGoogle
            .defaultOptions(OllamaChatOptions.builder()
                    .numPredict(createMessageRequest.maxTokens())
                    .temperature(createMessageRequest.temperature())
                )
            .build();
        String samplingAnswer = samplingClient
            .prompt()
            .system(createMessageRequest.systemPrompt())
            .user(
                createMessageRequest
                    .messages()
                    .getFirst()
                    .content()
                    .toString()
            )
            .call()
            .content();
        return McpSchema.CreateMessageResult.builder()
                .content(new McpSchema.TextContent(samplingAnswer))
                .build();
    }


    public void printAnswerToUserOllama(String question) throws JsonProcessingException {
        AssistantMessage assistantMessage = chatClientOllama.prompt().user(question).call().chatResponse().getResult().getOutput();
        System.out.println(assistantMessage.getText());

    }
    public void printAnswerToUserGoogle(String question) throws JsonProcessingException {
        AssistantMessage assistantMessage = chatClientGoogle.prompt().user(question).call().chatResponse().getResult().getOutput();
        System.out.println(assistantMessage.getText());

    }
}
