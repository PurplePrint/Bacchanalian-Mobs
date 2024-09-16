package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.effects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class EffectsConfig {
    private static final String CONFIG_FILE_NAME = "points_per_effects.json";
    private final File configPath;
    private final Map<String, Map<String, Map<String, EffectData>>> effectMap = new HashMap<>();

    public EffectsConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    private void loadConfig() {
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }

        try (Reader reader = Files.newBufferedReader(configPath.toPath())) {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();

            for (Map.Entry<String, JsonElement> dimensionEntry : jsonObject.entrySet()) {
                String dimension = dimensionEntry.getKey();
                JsonObject biomeObject = dimensionEntry.getValue().getAsJsonObject();

                for (Map.Entry<String, JsonElement> biomeEntry : biomeObject.entrySet()) {
                    String biomeId = biomeEntry.getKey();
                    JsonObject effectsObject = biomeEntry.getValue().getAsJsonObject();

                    for (Map.Entry<String, JsonElement> effectEntry : effectsObject.entrySet()) {
                        String effectId = effectEntry.getKey();
                        JsonObject effectDataObject = effectEntry.getValue().getAsJsonObject();

                        short minLevel = effectDataObject.get("MinimumLevel").getAsShort();
                        short maxLevel = effectDataObject.get("MaximumLevel").getAsShort();
                        short targetLevel = effectDataObject.get("TargetLevel").getAsShort();
                        Object targetPointsLevel = parseDoubleOrArray(effectDataObject.get("TargetPointsLevel"));

                        int minDuration = effectDataObject.get("MinimumDuration").getAsInt();
                        int maxDuration = effectDataObject.get("MaximumDuration").getAsInt();
                        int targetDuration = effectDataObject.get("TargetDuration").getAsInt();
                        Object targetPointsDuration = parseDoubleOrArray(effectDataObject.get("TargetPointsDuration"));

                        EffectData effectData = new EffectData(minLevel, maxLevel, targetLevel, targetPointsLevel,
                                minDuration, maxDuration, targetDuration, targetPointsDuration);

                        effectMap
                                .computeIfAbsent(dimension, k -> new HashMap<>())
                                .computeIfAbsent(biomeId, k -> new HashMap<>())
                                .put(effectId, effectData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object parseDoubleOrArray(JsonElement element) {
        try {
            return element.getAsDouble();
        } catch (NumberFormatException | UnsupportedOperationException e) {
            return element.getAsString();
        }
    }

    public EffectData getEffectData(short dimension, String biomeId, String effectId) {
        Map<String, Map<String, EffectData>> dimensionMap = effectMap.getOrDefault(String.valueOf(dimension), effectMap.get("ALL"));
        if (dimensionMap == null) return null;

        Map<String, EffectData> biomeMap = dimensionMap.getOrDefault(biomeId, dimensionMap.get("ALL"));
        if (biomeMap == null) return null;

        EffectData effectData = biomeMap.get(effectId);
        if (effectData == null) return null;

        return effectData;
    }

    public static class EffectData {
        private short minimumLevel;
        private short maximumLevel;
        private short targetLevel;
        private Object targetPointsLevel;
        private double targetPointsLevelParsed = 0;
        private int minimumDuration;
        private int maximumDuration;
        private int targetDuration;
        private Object targetPointsDuration;
        private double targetPointsDurationParsed = 0;

        public EffectData(short minimumLevel, short maximumLevel, short targetLevel, Object targetPointsLevel,
                          int minimumDuration, int maximumDuration, int targetDuration, Object targetPointsDuration) {
            this.minimumLevel = minimumLevel;
            this.maximumLevel = maximumLevel;
            this.targetLevel = targetLevel;
            this.targetPointsLevel = targetPointsLevel;
            this.minimumDuration = minimumDuration;
            this.maximumDuration = maximumDuration;
            this.targetDuration = targetDuration;
            this.targetPointsDuration = targetPointsDuration;
        }

        public short getMinimumLevel() { return minimumLevel; }
        public short getMaximumLevel() { return maximumLevel; }
        public short getTargetLevel() { return targetLevel; }
        public Object getTargetPointsLevel() { return targetPointsLevel; }
        public void setTargetPointsLevelParsed(double amount) {
            this.targetPointsLevelParsed = amount;
        }
        public double getTargetPointsLevelParsed() {
            return this.targetPointsLevelParsed;
        }
        public int getMinimumDuration() { return minimumDuration; }
        public int getMaximumDuration() { return maximumDuration; }
        public int getTargetDuration() { return targetDuration; }
        public Object getTargetPointsDuration() { return targetPointsDuration; }
        public void setTargetPointsDurationParsed(double amount) {
            this.targetPointsDurationParsed = amount;
        }
        public double getTargetPointsDurationParsed() {
            return this.targetPointsDurationParsed;
        }
    }
}
