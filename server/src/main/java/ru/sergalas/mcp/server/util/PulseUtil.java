package ru.sergalas.mcp.server.util;

import java.util.Random;

public class PulseUtil {
    public static final Random RANDOM = new Random();

    public static Integer getPulse(String name){
        return RANDOM.nextInt(100)+1;
    }
}
