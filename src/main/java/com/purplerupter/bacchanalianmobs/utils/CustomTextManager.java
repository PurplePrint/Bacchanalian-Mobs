// my skill issues with multilingual text...
package com.purplerupter.bacchanalianmobs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class CustomTextManager {

    private static final Map<String, String> customTexts = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "custom_text.cfg";
    private static File configPath;

    public static void init(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }

        try {
            List<String> lines = Files.readAllLines(configPath.toPath());
            for (String line : lines) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        customTexts.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getText(String key, Object... params) {
        String template = customTexts.getOrDefault(key, key);
        return java.text.MessageFormat.format(template, params);
    }
}
