package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.CommonConfig;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class ScalingPropsConfig {
    private static final String CONFIG_FILE_NAME = "scaling_props_health.json";
    private static File configPath;

    private static final boolean debug = CommonConfig.allowDebugMessages;

    private Map<String, JsonObject> rules = new HashMap<>();

    public ScalingPropsConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig(configPath);
        if (debug) { System.out.println("The config path is: " + configPath); }
    }

    private void loadConfig(File configFile) {
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }
        try (FileReader reader = new FileReader(configFile)) {
            JsonParser parser = new JsonParser();
            JsonObject rootObject = parser.parse(reader).getAsJsonObject();
            JsonArray rulesArray = rootObject.getAsJsonArray("rules");

            for (int i = 0; i < rulesArray.size(); i++) {
                JsonObject rule = rulesArray.get(i).getAsJsonObject();
                JsonArray mobIDs = rule.getAsJsonArray("mobID");

                for (int j = 0; j < mobIDs.size(); j++) {
                    rules.put(mobIDs.get(j).getAsString(), rule);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JsonObject getRuleForMob(String mobID, byte index) {
        if (rules == null || rules.isEmpty()) {
            if (debug) { System.out.println("The rules are empty or null!"); }
            return null;
        }

        if (debug) { System.out.println("The entrySet is:"); }
        if (debug) { System.out.println(rules.entrySet()); }

        for (Map.Entry<String, JsonObject> entry : rules.entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            JsonObject rule = entry.getValue();
            JsonArray mobIDs = rule.getAsJsonArray("mobID");
            if (debug) { System.out.println("The mobIDs is: " + mobIDs); }

            for (JsonElement idElement : mobIDs) {
                if (idElement.getAsString().equals(mobID)) {
                    if (debug) { System.out.println("Match mobID! It's: " + mobID); }
                    if (debug) { System.out.println("The rule is:"); }
                    if (debug) { System.out.println(rule); }
                    if (rule.has("ChangeByDifficulty")) {
                        if (debug) { System.out.println("That rule has \"ChangeByDifficulty\"!"); }
                        JsonObject changeByDifficulty = rule.getAsJsonObject("ChangeByDifficulty");
                        JsonElement targetMultiplier = changeByDifficulty.get("TargetMultiplier");

                        if (targetMultiplier.isJsonArray()) {
                            if (debug) { System.out.println("The targetMultipliers is json array"); }
                            JsonArray multipliers = targetMultiplier.getAsJsonArray();

                            if (index >= 0 && index < multipliers.size()) {
                                changeByDifficulty.addProperty("TargetMultiplier", multipliers.get(index).getAsFloat());
                            } else {
                                if (debug) { System.out.println("Error! Index for array is invalid"); }
                            }
                        } else {
                            if (debug) { System.out.println("The targetMultipliers is not json array"); }
                        }
                    }

                    if (rule.has("ChangeByTime")) {
                        JsonObject changeByTime = rule.getAsJsonObject("ChangeByTime");
                        JsonElement targetMultiplier = changeByTime.get("TargetMultiplier");

                        if (targetMultiplier.isJsonArray()) {
                            JsonArray multipliers = targetMultiplier.getAsJsonArray();

                            if (index >= 0 && index < multipliers.size()) {
                                changeByTime.addProperty("TargetMultiplier", multipliers.get(index).getAsFloat());
                            }
                        }
                    }

                    return rule;
                }
            }
        }

        return null;
    }
}
