package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.PassiveDifficultyChanger;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandRemovePassiveBonus extends CommandBase {

    @Override
    public String getName() {
        return "removepassivebonus";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/removepassivebonus <player> <identifier>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(new TextComponentString("Invalid arguments. Usage: " + getUsage(sender)));
            return;
        }

        String playerName = args[0];
        String identifier = args[1];

        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player == null) {
            sender.sendMessage(new TextComponentString("Player not found."));
            return;
        }

        PassiveDifficultyChanger passiveDifficultyChanger = DynamicDifficulty.passiveDifficultyChanger;
        passiveDifficultyChanger.removePassiveBonusPointsFromPlayer(player, identifier);
        sender.sendMessage(new TextComponentString("Removed passive bonus with identifier " + identifier + " from player " + playerName));
    }
}
