package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items.ItemCostConfig;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class CommandGetItemCost extends CommandBase {

    private final ItemCostConfig itemCostConfig;

    public CommandGetItemCost(ItemCostConfig itemCostConfig) {
        this.itemCostConfig = itemCostConfig;
    }

    @Override
    public String getName() {
        return "getitemcost";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/getitemcost <item_name> [index]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            World world = player.getEntityWorld();
            int playerY = (int) player.posY;
            Set<String> playerStages = new HashSet<>(GameStageHelper.getPlayerData(player).getStages());

            if (args.length < 1) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Error: You must specify an item name."));
                return;
            }

            String itemName = args[0];
            byte index = 0;

            if (args.length >= 2) {
                try {
                    index = Byte.parseByte(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Error: Index must be a valid number."));
                    return;
                }
            }

            byte currentPhase = 0;
            currentPhase = SRPSaveData.get(world).getEvolutionPhase(world.provider.getDimension());
            player.sendMessage(new TextComponentString("Y coordinate is: " + playerY));

            double itemCost = 0.0;
            try {
                itemCost = itemCostConfig.getItemCost(player, itemName, world, playerY, playerStages, currentPhase);
            } catch (NullPointerException e) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Error: Failed to retrieve item cost. Make sure the configuration is correct."));
                e.printStackTrace();
                return;
            }

            itemCost = itemCostConfig.getItemCost(player, itemName, world, playerY, playerStages, currentPhase);
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "The cost for " + itemName + " is: " + itemCost));
        } else {
            sender.sendMessage(new TextComponentString("This command can only be used by a player."));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Permission level for commands like /give, usually 2 for ops.
    }

    private ItemStack getItemStackFromString(String itemName) {
        ResourceLocation itemResourceLocation = new ResourceLocation(itemName);
        Item item = Item.REGISTRY.getObject(itemResourceLocation);

        if (item != null) {
            if (itemName.contains(":")) {
                String[] parts = itemName.split(":");
                if (parts.length == 3) {
                    try {
                        int meta = Integer.parseInt(parts[2]);
                        return new ItemStack(item, 1, meta);
                    } catch (NumberFormatException ignored) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
}
