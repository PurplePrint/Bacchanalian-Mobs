package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items;

import com.google.gson.*;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class ItemCostConfig {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    private final File configPath;
    private static final String CONFIG_FILE_NAME = "points_per_items.json";
    private JsonObject configData;

    public ItemCostConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    private void loadConfig() {
        try {
            if (!configPath.exists()) {
                createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
            }
            JsonParser parser = new JsonParser();
            configData = parser.parse(new FileReader(configPath)).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try (FileWriter writer = new FileWriter(configPath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(configData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getItemCost(EntityPlayerMP player, String itemName, World world, int y, Set<String> playerStages, byte index) {
        JsonObject itemCostData = configData.getAsJsonObject("ItemCost");
        double totalCost = 0.0;

        Set<String> itemGroups = getItemGroups(itemName);
        if (itemGroups.isEmpty()) {
            return totalCost;
        }

        for (String group : itemGroups) {
            if (!itemCostData.has(group)) continue;

            JsonObject groupData = itemCostData.getAsJsonObject(group);
            double cost = 0.0;

            JsonElement costElement = groupData.get("Cost");
            if (costElement.isJsonPrimitive()) {
                cost = costElement.getAsDouble();
            } else if (costElement.isJsonArray()) {
                index += 2; // minimum SRP phase is -2, minimum index in array is 0
                JsonArray costArray = costElement.getAsJsonArray();
                if (index >= 0 && index < costArray.size()) {
                    cost = costArray.get(index).getAsDouble();
                } else {
                    throw new IndexOutOfBoundsException("Invalid index for cost array: " + index);
                }
            }

            JsonArray dimensionRules = groupData.getAsJsonArray("ChangeByDimension");
            for (JsonElement ruleElement : dimensionRules) {
                JsonObject rule = ruleElement.getAsJsonObject();
                if (doesDimensionRuleApply(player, rule, world, y)) {
                    cost = applyOperation(cost, rule.get("operation").getAsString(), rule.get("amount").getAsDouble());
                }
            }

            JsonArray stageRules = groupData.getAsJsonArray("ChangeByGameStage");
            for (JsonElement ruleElement : stageRules) {
                JsonObject rule = ruleElement.getAsJsonObject();
                if (doesStageRuleApply(rule, playerStages)) {
                    cost = applyOperation(cost, rule.get("operation").getAsString(), rule.get("amount").getAsDouble());
                }
            }

            totalCost += cost;
        }

        return totalCost;
    }

    private Set<String> getItemGroups(String itemName) {
        JsonObject itemGroups = configData.getAsJsonObject("ItemGroups");
        Set<String> matchingGroups = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : itemGroups.entrySet()) {
            JsonArray items = entry.getValue().getAsJsonArray();
            for (JsonElement item : items) {
                if (item.getAsString().equals(itemName)) {
                    matchingGroups.add(entry.getKey());
                }
            }
        }
        return matchingGroups;
    }

    private boolean doesDimensionRuleApply(EntityPlayerMP player, JsonObject rule, World world, int y) {
//        short dimensionID = rule.has("dimension") ? rule.get("dimension").getAsShort() : Short.MIN_VALUE;
        short dimensionID = Short.MIN_VALUE;
        boolean allDimensions = false;
        if (rule.has("dimension")) {
            if (debug) { System.out.println("That json has 'dimension'!"); }
            try {
                dimensionID = rule.get("dimension").getAsShort();
            } catch (NumberFormatException e1) {
                if (debug) { System.out.println("\"dimension\" is not a numeric dimension ID. Try to parse it as \"ALL\"."); }
                try {
                    if (rule.get("dimension").getAsString().equals("ALL")) {
                        if (debug) { System.out.println("Apply this json config to all dimensions"); }
                        allDimensions = true;
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        JsonElement biomeIDElement = rule.get("biomeID");
        boolean allBiomes = false;
        String[] biomeIDs;
        if (biomeIDElement.isJsonArray()) {
            biomeIDs = new Gson().fromJson(biomeIDElement, String[].class);
        } else {
            if (biomeIDElement.getAsString().equals("ALL")) {
                if (debug) { System.out.println("All biomes"); }
                allBiomes = true;
                biomeIDs = null;
            } else {
                biomeIDs = new String[]{biomeIDElement.getAsString()};
            }
        }
        if (debug) { System.out.println("biomeIDs: " + biomeIDs); }

        int[] yRange = parseYRange(rule.getAsJsonArray("yRange"));

        boolean biomeMatches = false;
        if (!allBiomes) {
            biomeMatches = (containsBiome(world.getBiome(player.getPosition()).getRegistryName().toString(), biomeIDs));
        }

//        if ((dimensionID == Short.MIN_VALUE || dimensionID == world.provider.getDimension()) &&
        if (allDimensions || dimensionID == world.provider.getDimension() &&
                (allBiomes || biomeMatches) &&
                (yRange[0] <= y && y <= yRange[1])) {
            return true;
        }
        return false;
    }

    private boolean containsBiome(String biome, String[] biomes) {
        for (String b : biomes) {
            if (b.equals(biome)) return true;
        }
        return false;
    }

    private boolean doesStageRuleApply(JsonObject rule, Set<String> playerStages) {
        String stage = rule.get("stage").getAsString();
        boolean hasStage = gameStageHaveOrNot(stage);

        if ((hasStage && playerStages.contains(stage)) || (!hasStage && !playerStages.contains(stage))) {
            return true;
        }
        return false;
    }

    private int[] parseYRange(JsonArray rangeArray) {
        int minY = rangeArray.get(0).getAsString().equals("x") ? Integer.MIN_VALUE : rangeArray.get(0).getAsInt();
        int maxY = rangeArray.get(1).getAsString().equals("x") ? Integer.MAX_VALUE : rangeArray.get(1).getAsInt();
        return new int[]{minY, maxY};
    }

    private boolean gameStageHaveOrNot(String stageString) {
        return stageString.startsWith("+");
    }

    private double applyOperation(double cost, String operation, double amount) {
        switch (operation) {
            case "ADD":
                return cost + amount;
            case "MULT":
                return cost * amount;
            default:
                return cost;
        }
    }
}
