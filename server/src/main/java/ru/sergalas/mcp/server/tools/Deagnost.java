package ru.sergalas.mcp.server.tools;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static ru.sergalas.mcp.server.util.MedicalProfileUtil.getMedicalProfile;
import static ru.sergalas.mcp.server.util.PulseUtil.getPulse;

@Service
public class Deagnost {
    private static final String SAMPLING_SYSTEM_PROMPT = """
            Ты ставишь диагноз одним словом.
             На вход всегда получаешь медицинскую карту человека и его текущий пульс.
             Твоя задача — выдать ровно одно:
            название существующей болезни (может быть 1–3 слова, можно редкие или забавно звучащие),
             или
            
            
            Ответ: -сказать что пациент здоров.
            Правила:
             — Анализируй карту пациента и пульс и выбирай подходящую болезнь.
             — Отвечай только названием болезни или фразой что пациент здоров.
             — Никаких пояснений, никакого текста вокруг.
            
            """;

    @McpTool(
            name = "diagnost",
            title = "Диагностика по имени",
            description = "Используется для получения диагноза по имени человека. Всегда возвращает либо название болезни, либо сообщение, что человек ничем не болеет."
    )
    public String callDiagnost(
            McpSyncRequestContext requestContext,
            @McpToolParam(description = "Имя пациента, по которому требуется определить текущий диагноз.") String name
    ) {
        Integer pulse = getPulse(name);
        String medicalProfile = getMedicalProfile(name);
        String samplingPrompt = "вот такой у нас пациент, вот его медицинская карта +" + medicalProfile + " а вот его текущий пульс: " + pulse;
        McpSchema.CreateMessageRequest samplingMessageRequest = McpSchema.CreateMessageRequest.builder()
            .systemPrompt(SAMPLING_SYSTEM_PROMPT)
            .temperature(0.1)
            .maxTokens(100)
            .messages(
                List.of(
                        new McpSchema.SamplingMessage(
                                McpSchema.Role.USER,
                                new McpSchema.TextContent(samplingPrompt)
                        )
                )
            )
            .build();
        McpSchema.CreateMessageResult samplingResponseMessage = requestContext.sample(samplingMessageRequest);

        return samplingResponseMessage.toString();
    }

}
