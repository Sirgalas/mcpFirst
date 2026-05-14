package ru.sergalas.mcp.server;


import lombok.SneakyThrows;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.sergalas.mcp.server.tools.Allergolog;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class App
{

    @Bean
    public List<ToolCallback> toolCallbacks()
    {
        return  List.of(ToolCallbacks.from(new Allergolog()));
    }

    @SneakyThrows
    public static void main( String[] args )
    {
        System.out.println("Spring server start");
        SpringApplication.run(App.class,args);
    }
}
