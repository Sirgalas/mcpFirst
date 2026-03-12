package ru.sergalas.mcp.server;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;

public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Start server" );
        HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider
                .builder()
                .mcpEndpoint("/mcp")
                .build();
        McpServer.sync(transportProvider).build();
    }
}
