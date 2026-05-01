package ru.sergalas.hosting.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Pattern;

public class CallToolUtil {
    private final static ObjectMapper ma
            = new ObjectMapper();

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile("<tool_call>\\s*(\\{.*?})\\s*</tool_call>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
}
