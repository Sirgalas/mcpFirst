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

import java.util.HashMap;
import java.util.Map;

public class App
{
    @SneakyThrows
    public static void main( String[] args )
    {
        System.out.println( "Start server" );
        HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider
                .builder()
                .mcpEndpoint("/mcp")
                .build();

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
                .tools(bioSensorToolSpec)
                .build();

        Server server = new Server(8091);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.addServlet(new ServletHolder(transportProvider), "/*");

        server.setHandler(contextHandler);

        server.start();
        server.join();
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
