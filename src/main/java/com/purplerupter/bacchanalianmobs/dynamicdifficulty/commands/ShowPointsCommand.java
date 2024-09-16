package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.ShowPoints;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class ShowPointsCommand extends CommandBase {

    @Override
    public String getName() {
        return "showpoints";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/showpoints";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {

            EntityPlayer player = (EntityPlayer) sender;
            if (ShowPoints.returnPointsAmount(player) == 0.0f) {
                player.sendMessage(new TextComponentString("No difficulty points data found."));
            } else {
                player.sendMessage(new TextComponentString("Your difficulty points: " + ShowPoints.returnPointsAmount(player)));
            }
        }
    }

    public void register(FMLServerStartingEvent event) {
        event.registerServerCommand(new ShowPointsCommand());
    }
}
