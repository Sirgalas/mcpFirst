package ru.sergalas.mcp.server.tools;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Sensor {

    @McpTool(
            name = "bioSensor",
            title = "Human Virtual Bio Sensor",
            description = "Retrieves and analyzes real-time or historical biometric sensor data (e.g., heart rate, body temperature, SpO2, stress index). Supports filtering by device ID, time range, and specific physiological metrics. Returns structured readings suitable for monitoring, alerting, or clinical/analytics pipelines.+"
    )
    public Map<String, Object> callSensor(Integer numberOfDays) {
        return resultAnswer(numberOfDays);
    }

    private Map<String, Object> resultAnswer(Integer days) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("pulse","пульс пользователя за последних %s дня, был 42 удара в минуту".formatted(days));
        properties.put("state", "Все в порядке");
        properties.put("sleepDeprivation",true);

        return properties;
    }
}
