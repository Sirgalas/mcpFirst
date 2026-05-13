package ru.sergalas.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;  // ✅ С ee10!
import org.eclipse.jetty.ee10.servlet.ServletHolder;

import static ru.sergalas.mcp.server.util.PulseUtil.getPulse;
import static ru.sergalas.mcp.server.util.MedicalProfileUtil.getMedicalProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App
{

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


    @SneakyThrows
    public static void main( String[] args )
    {
        System.out.println( "Start server" );
        HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider
                .builder()
                .mcpEndpoint("/mcp")
                .build();
        /* Диагност */
        McpSchema.Tool diagnostTool = McpSchema.Tool
                .builder()
                .name("diagnost")
                .title("Диагностика по имени")
                .description("Используется для получения диагноза по имени человека. Всегда возвращает либо название болезни, либо сообщение, что человек ничем не болеет.")
                .inputSchema(new JacksonMcpJsonMapper(new ObjectMapper()), creareDiagnoctInputSchema())
                .build();

        McpServerFeatures.SyncToolSpecification diagnostToolSpec = McpServerFeatures.SyncToolSpecification.builder()
            .tool(diagnostTool)
            .callHandler(((mcpSyncServerExchange, callToolRequest) -> {
                System.out.println("Сервер спросил у клиента может он делать sampling: %s".formatted(mcpSyncServerExchange.getClientCapabilities().sampling()));
                String name = callToolRequest.arguments().get("name").toString();
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
                McpSchema.CreateMessageResult samplingResponseMessage = mcpSyncServerExchange.createMessage(samplingMessageRequest);

                mcpSyncServerExchange.loggingNotification(
                    McpSchema.LoggingMessageNotification.builder()
                        .data("сервер запросил сэмплингом %s \n сервер получил ответ %s".formatted(samplingPrompt, samplingResponseMessage.content()))
                        .build()
                );

                return McpSchema.CallToolResult.builder().addContent(samplingResponseMessage.content()).build();
                })
            ).build();

        /* Биосенсор */
        McpSchema.Tool bioSensorTool = McpSchema.Tool.builder()
            .name("bioSensor")
            .title("Human Virtual Bio Sensor")
            .description("Retrieves and analyzes real-time or historical biometric sensor data (e.g., heart rate, body temperature, SpO2, stress index). Supports filtering by device ID, time range, and specific physiological metrics. Returns structured readings suitable for monitoring, alerting, or clinical/analytics pipelines.")
            .inputSchema(new JacksonMcpJsonMapper(new ObjectMapper()), createBioSensorInputSchema())
            .outputSchema(new JacksonMcpJsonMapper(new ObjectMapper()), createBioSensorOutputSchema())
            .build();

        McpServerFeatures.SyncToolSpecification bioSensorToolSpec = McpServerFeatures.SyncToolSpecification.builder()
            .tool(bioSensorTool)
            .callHandler(
                (mcpSyncServerExchange, callToolRequest) ->
                {
                    String serverLogMessage = "Ответ от сервера %s".formatted(callToolRequest.toString());
                    mcpSyncServerExchange.loggingNotification(
                            McpSchema.LoggingMessageNotification
                                .builder()
                                .data(serverLogMessage)
                                .build()
                        );
                    Integer days = (Integer) callToolRequest.arguments().get("days");
                    return resultAnswer(days);                }
            )
            .build();

        McpServer
                .sync(transportProvider)
                .serverInfo("mcpulse test server","1.0.REALISE")
                .capabilities(createServerCapabilities())
                .tools(bioSensorToolSpec, diagnostToolSpec)
                .build();

        Server server = new Server(8091);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(transportProvider), "/*");

        server.setHandler(contextHandler);

        server.start();
        server.join();
    }

    private static String creareDiagnoctInputSchema() {
        ObjectNode rootDiagnost = new ObjectMapper().createObjectNode().put("type", "object");
        rootDiagnost.putObject("properties")
            .putObject("name")
                .put("type", "string")
                .put("description", "Имя пациента, по которому требуется определить текущий диагноз.");
        rootDiagnost.putArray("required").add("name");
        return  rootDiagnost.toString();
    }

    private static McpSchema.CallToolResult resultAnswer(Integer days) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("pulse","пульс пользователя за последних %s дня, был 42 удара в минуту".formatted(days));
        properties.put("state", "Все в порядке");
        properties.put("sleepDeprivation",true);

        return McpSchema.CallToolResult.builder()
                .structuredContent(properties)
                .build();
    }

    private static String createBioSensorOutputSchema() {
        ObjectNode rootNode = new ObjectMapper().createObjectNode().put("type", "object");
        ObjectNode properties = rootNode.putObject("properties");
        properties.putObject("pulse")
                .put("type", "string")
                .put("description","average pulse rate for last days");
        properties.putObject("state")
                .put("type", "string")
                .put("description","what state of user");
        properties.putObject("sleepDeprivation")
                .put("type", "boolean")
                .put("description","sleep deprivation yes or no");

        return rootNode.toString();
    }

    private static String createBioSensorInputSchema() {
        ObjectNode rootNode = new ObjectMapper().createObjectNode().put("type", "object");
        rootNode
            .putObject("properties")
            .putObject("days")
            .put("type","integer")
            .put("description","Number of past day to include in the pulse reading request")
            .putArray("required")
                .add("days");
        return rootNode.toString();
    }

    private static McpSchema.ServerCapabilities createServerCapabilities() {
        return McpSchema.ServerCapabilities.builder().tools(true).build();
    }
}
