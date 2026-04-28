package ru.sergalas.mcp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;  // ✅ С ee10!
import org.eclipse.jetty.ee10.servlet.ServletHolder;

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
                .inputSchema(new JacksonMcpJsonMapper(new ObjectMapper()), createBioSensorShema())
                .build();

        McpServerFeatures.SyncToolSpecification bioSensorToolSpec = McpServerFeatures.SyncToolSpecification.builder()
                .tool(bioSensorTool)
                .callHandler((mcpSyncServerExchange, callToolRequest) -> new McpSchema.CallToolResult("пульс пользователя 42", false))
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

    private static String createBioSensorShema() {
        return new ObjectMapper().createObjectNode().put("type", "object").toString();
    }

    private static McpSchema.ServerCapabilities createServerCapabilities() {
        return McpSchema.ServerCapabilities.builder().tools(true).build();
    }
}
