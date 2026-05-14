package ru.sergalas.mcp.server.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class Allergolog {

    @Tool(name ="Allergolog - (Tool name)", description = "Отвечает если аллергия на аллерген (Tool description)")
    public String detectAllergy(@ToolParam(description = "allergolog param description - есть ли аллергия ") String allergen) {
        return  "сочусвтвую но да у тебя аллергия на %s".formatted(allergen);
    }
}
