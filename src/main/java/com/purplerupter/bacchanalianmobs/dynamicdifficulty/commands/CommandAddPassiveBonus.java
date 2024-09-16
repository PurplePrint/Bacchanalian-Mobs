package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.PassiveDifficultyChanger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandAddPassiveBonus extends CommandBase {

    @Override
    public String getName() {
        return "addpassivebonus";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/addpassivebonus <player> <identifier> <value>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(new TextComponentString("Invalid arguments. Usage: " + getUsage(sender)));
            return;
        }

        String playerName = args[0];
        String identifier = args[1];
        double value;

        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString("Value must be a number."));
            return;
        }

        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player == null) {
            sender.sendMessage(new TextComponentString("Player not found."));
            return;
        }

        PassiveDifficultyChanger passiveDifficultyChanger = DynamicDifficulty.passiveDifficultyChanger;
        passiveDifficultyChanger.addPassiveBonusPointsToPlayer(player, identifier, value);
        sender.sendMessage(new TextComponentString("Added passive bonus " + value + " with identifier " + identifier + " to player " + playerName));
    }
}
