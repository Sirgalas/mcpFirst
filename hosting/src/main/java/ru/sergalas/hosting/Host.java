package ru.sergalas.hosting;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.sergalas.hosting.util.CallToolUtil;

@Service
public class Host {

    @Autowired
    ChatClient chatClient;


    private String systemPrompt;
    private McpSyncClient client;

    @PostConstruct
    public void init() {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder("http://localhost:8091").endpoint("mcp").build();
        client = McpClient.sync(transport).build();
        client.initialize();
        McpSchema.ListToolsResult toolsResult = client.listTools();
        systemPrompt = SystemPromptFactory.withTools(toolsResult);
    }


    public void printAnswerToUser(String question){
        AssistantMessage assistantMessage = chatClient.prompt().system(systemPrompt).user(question).call().chatResponse().getResult().getOutput();
        if(CallToolUtil.isToolRequired(assistantMessage.getText())){}

    }
}
