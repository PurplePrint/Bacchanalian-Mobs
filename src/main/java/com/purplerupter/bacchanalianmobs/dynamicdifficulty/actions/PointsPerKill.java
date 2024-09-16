// TODO: SRP phases support in config!

package com.purplerupter.bacchanalianmobs.dynamicdifficulty.actions;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.ChangePoints;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class PointsPerKill {

    private static File configPath;
    private static final String CONFIG_FILE_NAME = "points_per_kill.cfg";
    private final Map<String, Map<Short, Double>> pointsPerKill = new HashMap<>();

    public PointsPerKill(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    private void loadConfig() {
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }

        try {
            List<String> lines = Files.readAllLines(configPath.toPath());
//            System.out.println("Config lines: " + lines);

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 3) {
                    continue;
                }

                String mobId = parts[0];
                short dimensionId = parts[1].equalsIgnoreCase("ALL") ? Short.MIN_VALUE : Short.parseShort(parts[1]);
                double points = Integer.parseInt(parts[2]);

//                System.out.println("Parsed config: " + mobId + ", " + dimensionId + ", " + points);
                pointsPerKill.computeIfAbsent(mobId, k -> new HashMap<>()).put(dimensionId, points);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getEntityWorld();
        Entity source = event.getSource().getTrueSource();

        if (source instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) source;
            String mobId = EntityList.getKey(entity).toString();
            short dimensionId = (short) world.provider.getDimension();

            if (mobId != null && pointsPerKill.containsKey(mobId)) {
                Map<Short, Double> dimensionPoints = pointsPerKill.get(mobId);
                double points = dimensionPoints.getOrDefault(dimensionId, dimensionPoints.getOrDefault(Double.MIN_VALUE, 0.0));

                // TODO: different colors for neg/pos points!
//                if (points > 0) {
//                    ChangePoints.changeDifficultyPoints(player, points, true);
//                }
                ChangePoints.changeDifficultyPoints(player, points, true);
            }
        }
    }
}
