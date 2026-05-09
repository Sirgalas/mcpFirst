package ru.sergalas.hosting.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallToolUtil {
    private final static ObjectMapper objectMapper
            = new ObjectMapper();

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile("<tool_call>\\s*(\\{.*?})\\s*</tool_call>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public static String wrapResponse(String toolResult) {
        return String.format("<tool_response>%s</tool_response>", toolResult);
    }

    public static boolean isToolRequired(String text) {
        return TOOL_CALL_PATTERN.matcher(text).find();
    }

    public static McpSchema.CallToolRequest getRequiredTool(String text) throws JsonProcessingException {
        Matcher matcher = TOOL_CALL_PATTERN.matcher(text);
        matcher.find();
        String toolCallRequestJson = matcher.group(1).trim();
        JsonNode tool = objectMapper.readTree(toolCallRequestJson);
        String toolName = tool.path("name").asText();
        JsonNode parameters = tool.path("parameters");


        McpSchema.CallToolRequest.Builder builder = McpSchema.CallToolRequest.builder().name(toolName);
        
        JsonNode parametersNode = tool.path("parameters");
        if (!parametersNode.isMissingNode() && !parametersNode.isNull()) {
            Map<String, Object> arguments = objectMapper.convertValue(parameters, new TypeReference<>() {});
            builder.arguments(objectMapper.convertValue(arguments, Map.class));
        }
        
        return builder.build();
    }
}