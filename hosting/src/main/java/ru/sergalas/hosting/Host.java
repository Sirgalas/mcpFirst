package ru.sergalas.hosting;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sergalas.hosting.util.CallToolUtil;

import java.util.List;

@Service
public class Host {

    @Autowired
    ChatModel chatModel;

    @Autowired
    ChatClient chatClient;


    private String systemPrompt;
    private McpSyncClient client;

    @PostConstruct
    public void init() {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:8091").endpoint("mcp").build();
        client = McpClient
                .sync(transport)
                .sampling(createMessageRequest -> {
                    ChatClient samplingClient = ChatClient.builder(chatModel)
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
                    return  McpSchema.CreateMessageResult.builder()
                        .content(new McpSchema.TextContent(samplingAnswer))
                        .build();
                })
                .loggingConsumer(loggingMessageNotification -> System.out.println(loggingMessageNotification.data()) )
                .capabilities(McpSchema.ClientCapabilities.builder().sampling().build())
                .build();
        client.initialize();
        McpSchema.ListToolsResult toolsResult = client.listTools();
        systemPrompt = SystemPromptFactory.withTools(toolsResult);
    }


    public void printAnswerToUser(String question) throws JsonProcessingException {
        AssistantMessage assistantMessage = chatClient.prompt().system(systemPrompt).user(question).call().chatResponse().getResult().getOutput();
        if(CallToolUtil.isToolRequired(assistantMessage.getText())){
            McpSchema.CallToolRequest toolResult = CallToolUtil.getRequiredTool(assistantMessage.getText());

            String toolResponse = CallToolUtil.wrapResponse(client.callTool(toolResult).content().getFirst().toString());

            UserMessage toolMessage = new UserMessage(toolResponse);
            UserMessage userMessage = new UserMessage(question);

            assistantMessage = chatClient.prompt().system(systemPrompt).messages(List.of(userMessage,assistantMessage,toolMessage)).call().chatResponse().getResult().getOutput();

        }

        System.out.println(assistantMessage.getText());

    }
}
