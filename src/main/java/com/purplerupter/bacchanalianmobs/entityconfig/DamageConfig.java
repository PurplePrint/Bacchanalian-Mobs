package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.CommonConfig;

import java.io.*;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class DamageConfig {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    private static final String DAMAGE_CONFIG_FILENAME = "damage_config.json";
    private static File damageConfigPath;
    private JsonObject configData;

    public DamageConfig(File configDir) {
        damageConfigPath = new File(configDir, DAMAGE_CONFIG_FILENAME);
        if (!damageConfigPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + DAMAGE_CONFIG_FILENAME, damageConfigPath);
        }
        loadConfig();
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(damageConfigPath)) {
//            configData = JsonParser.parseReader(reader).getAsJsonObject(); // java.lang.NoSuchMethodError
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getDamageSourceRule(String mobID, String damageSource, byte index) {
        JsonObject damageSourceMultipliers = configData.getAsJsonObject("DamageSourceMultipliers");
//        for (String rule : damageSourceMultipliers.keySet()) { // java.lang.NoSuchMethodError
//            JsonObject ruleObject = damageSourceMultipliers.getAsJsonObject(rule);
        for (Map.Entry<String, JsonElement> entry : damageSourceMultipliers.entrySet()) {
            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            JsonArray mobs = ruleObject.getAsJsonArray("mobID");
            if (arrayContains(mobs, mobID)) {
                JsonObject damageChanges = ruleObject.getAsJsonObject("DamageChanges");
                if (damageChanges.has(damageSource)) {
                    // if 13 float numbers (from -2 to 10 SRP phase)
                    if (damageChanges.get(damageSource).isJsonArray()) {
                        return extractIndexedDamageRule(ruleObject, damageSource, index);
                    } else {
                        return ruleObject;
                    }
                }
            }
        }
        return null;
    }

    public JsonObject getEntityDamageRule(String attackerID, String victimID, byte index) {
        JsonObject entityDamageMultipliers = configData.getAsJsonObject("EntityDamageMultipliers");
//        for (String rule : entityDamageMultipliers.keySet()) {
//            JsonObject ruleObject = entityDamageMultipliers.getAsJsonObject(rule);
        for (Map.Entry<String, JsonElement> entry : entityDamageMultipliers.entrySet()) {
            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            if (arrayContains(ruleObject.getAsJsonArray("attackerID"), attackerID)
                    && arrayContains(ruleObject.getAsJsonArray("victimID"), victimID)) {
                // if 13 float numbers (from -2 to 10 SRP phase)
                if (ruleObject.get("multiplier").isJsonArray()) {
                    ruleObject.addProperty("multiplier", ruleObject.getAsJsonArray("multiplier").get(index).getAsFloat());
                }
                return ruleObject;
            }
        }
        return null;
    }

    private boolean arrayContains(JsonArray array, String value) {
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getAsString().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonObject extractIndexedDamageRule(JsonObject ruleObject, String damageSource, byte index) {
        JsonArray damageArray = ruleObject.getAsJsonObject("DamageChanges").getAsJsonArray(damageSource);
        ruleObject.getAsJsonObject("DamageChanges").addProperty(damageSource, damageArray.get(index).getAsFloat());
        return ruleObject;
    }
}
