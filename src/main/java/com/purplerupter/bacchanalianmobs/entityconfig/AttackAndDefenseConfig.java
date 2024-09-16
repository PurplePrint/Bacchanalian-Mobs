package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.*;
import com.purplerupter.bacchanalianmobs.CommonConfig;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class AttackAndDefenseConfig {
    private static final String attackAndDefenseConfigFileName = "attack_and_defense_config.json";
    private static File attackAndDefenseConfigPath;
    private JsonObject configData;
    private Gson gson;

    private static final boolean debug = CommonConfig.allowDebugMessages;

    public AttackAndDefenseConfig(File configDir) {
        attackAndDefenseConfigPath = new File(configDir, attackAndDefenseConfigFileName);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    private void loadConfig() {
        if (attackAndDefenseConfigPath.exists()) {
            if (debug) { System.out.println(attackAndDefenseConfigPath + " exist!"); }
            try (FileReader reader = new FileReader(attackAndDefenseConfigPath)) {
                if (debug) { System.out.println("try to parse..."); }
                this.configData = gson.fromJson(reader, JsonObject.class);
                if (debug) { System.out.println("success"); }
            } catch (Exception e) {
                if (debug) { System.out.println("It's sad"); }
                e.printStackTrace();
            }
        } else {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + attackAndDefenseConfigFileName, attackAndDefenseConfigPath);
        }
    }

    private JsonArray createDefaultIntArray(int size, int defaultValue) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < size; i++) {
            array.add(defaultValue);
        }
        return array;
    }

    private JsonArray createRangeArray(int start, int end) {
        JsonArray array = new JsonArray();
        array.add(start);
        array.add(end);
        return array;
    }

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(attackAndDefenseConfigPath)) {
            gson.toJson(this.configData, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject getMobOffenseRule(String mobID, byte index) {
        return getRule("MobOffense", mobID, index);
    }

    public JsonObject getMobDefenseRule(String mobID, byte index) {
        return getRule("MobDefense", mobID, index);
    }

    private JsonObject getRule(String section, String mobID, byte index) {
        if (configData.has(section)) {
            if (debug) { System.out.println("has section and it is a: " + section); }
            JsonArray rules = configData.getAsJsonArray(section);
            for (JsonElement element : rules) {
                if (debug) { System.out.println("that rule element is: " + element); }
                JsonObject rule = element.getAsJsonObject();
                if (matchesMobID(rule, mobID)) {
                    if (debug) { System.out.println("matchesMobID!"); }
                    return processRule(rule, index);
                }
            }
        }
        return null;
    }

    private boolean matchesMobID(JsonObject rule, String mobID) {
        JsonElement mobIDElement = rule.get("mobID");
        if (mobIDElement != null) {
            if (mobIDElement.isJsonArray()) {
                for (JsonElement idElement : mobIDElement.getAsJsonArray()) {
                    if (idElement.getAsString().equals(mobID)) {
                        return true;
                    }
                }
            } else if (mobIDElement.getAsString().equals(mobID)) {
                return true;
            }
        }
        return false;
    }

    private JsonObject processRule(JsonObject rule, byte index) {
        JsonObject processedRule = new JsonObject();
        if (rule.has("Damage")) {
            processedRule.add("Damage", processDamageOrEffect(rule.getAsJsonObject("Damage"), index));
        }
        if (rule.has("Effects")) {
            processedRule.add("Effects", processDamageOrEffect(rule.getAsJsonObject("Effects"), index));
        }
        if (rule.has("Conditions")) {
            processedRule.add("Conditions", rule.getAsJsonObject("Conditions"));
        }
        if (rule.has("Multipliers")) {
            processedRule.add("Multipliers", rule.getAsJsonObject("Multipliers"));
        }
        return processedRule;
    }

    private JsonObject processDamageOrEffect(JsonObject source, byte index) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonArray() && value.getAsJsonArray().size() == 13) {
                result.addProperty(key, value.getAsJsonArray().get(index).getAsInt());
            } else {
                result.add(key, value);
            }
        }
        return result;
    }
}
