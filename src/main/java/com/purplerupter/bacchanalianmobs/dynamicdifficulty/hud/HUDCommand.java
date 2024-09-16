package com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class HUDCommand extends CommandBase {

    @Override
    public String getName() {
        return "difficultyhud";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/difficultyhud <x/y coordinate> <plus/minus direction> <shift distance from 0 to 100 floating point>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(new TextComponentString("Invalid arguments. Usage: " + getUsage(sender)));
            return;
        }

        char coordinate;
        String direction = args[1];
        float distance;

        if (args[0].length() == 1) {
            coordinate = args[0].charAt(0);
        } else {
            sender.sendMessage(new TextComponentString("Coordinate must be 'x' or 'y'."));
            return;
        }

        switch (direction) {
            case "plus":
            case "minus":
                break;
            default:
                sender.sendMessage(new TextComponentString("Direction must be 'plus' or 'minus'."));
                return;
        }

        try {
            distance = Float.parseFloat(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString("Distance must be a number."));
            return;
        }

        if (distance < 0) {
            sender.sendMessage(new TextComponentString("Distance cannot be negative."));
            return;
        }
        if (distance > 100) {
            sender.sendMessage(new TextComponentString("Distance cannot be greater than 100 percent."));
            return;
        }

        switch (coordinate) {
            case 'x':
                if (direction.equals("plus")) {
                    HUDConfig.customPosX += distance;
                    HUDConfig.saveConfig();
                } else {
                    HUDConfig.customPosX -= distance;
                    HUDConfig.saveConfig();
                }
                break;
            case 'y':
                if (direction.equals("plus")) {
                    HUDConfig.customPosY += distance;
                    HUDConfig.saveConfig();
                } else {
                    HUDConfig.customPosY -= distance;
                    HUDConfig.saveConfig();
                }
                break;
            default:
                sender.sendMessage(new TextComponentString("Coordinate must be 'x' or 'y'."));
        }
    }

}
