package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive;

import java.util.HashMap;
import java.util.Map;

public class ArmorStats {
    private static final Map<String, Integer> playerDefenseLevels = new HashMap<>();
    private static final Map<String, Integer> playerHardnessLevels = new HashMap<>();

    public static void setDefenseLevel(String playerName, int defenseLevel) {
        playerDefenseLevels.put(playerName, defenseLevel);
    }

    public static void setHardnessLevel(String playerName, int hardnessLevel) {
        playerHardnessLevels.put(playerName, hardnessLevel);
    }

    public static int getDefenseLevel(String playerName) {
        return playerDefenseLevels.getOrDefault(playerName, 0);
    }

    public static int getToughnessLevel(String playerName) {
        return playerHardnessLevels.getOrDefault(playerName, 0);
    }
}
