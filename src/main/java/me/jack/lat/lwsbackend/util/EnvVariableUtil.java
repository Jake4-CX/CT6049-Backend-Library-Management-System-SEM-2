package me.jack.lat.lwsbackend.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvVariableUtil {

    private static final Dotenv dotenv = Dotenv.configure().load();

    public static String getVariable(String variableName) {
        return dotenv.get(variableName);
    }
}
