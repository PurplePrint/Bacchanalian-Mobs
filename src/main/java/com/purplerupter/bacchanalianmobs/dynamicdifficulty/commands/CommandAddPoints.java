package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.ChangePoints;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class CommandAddPoints extends CommandBase {
    @Override
    public String getName() {
        return "addpoints";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/addpoints <player> <amount>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString("Invalid arguments. Usage: " + getUsage(sender)));
            return;
        }

        try {
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(args[0]);
            if (player == null) {
                sender.sendMessage(new TextComponentString("Error! Player with name " + args[0] + " does not exist or not online!"));
                return;
            }
        } catch (NullPointerException e) {
            sender.sendMessage(new TextComponentString("Error! Player with name " + args[0] + " does not exist or not online!"));
            return;
        }

        float amount;
        try {
            amount = Float.parseFloat(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString("Amount must be a number."));
            return;
        }

//        ActionDifficultyChanger.changePlayerPoints(args[0], amount);
        ChangePoints.changeDifficultyPoints(args[0], amount);
        sender.sendMessage(new TextComponentString("Player with name " + args[0] + " get " + amount + " points."));
    }
}
