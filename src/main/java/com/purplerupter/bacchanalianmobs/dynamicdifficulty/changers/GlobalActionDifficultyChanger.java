package com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.utils.CustomTextManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;

public class GlobalActionDifficultyChanger {

    // Don't Repeat Yourself!
    public static void universalChanger(EntityPlayerMP player, double amount) {
        ChangePoints.changeDifficultyPoints(player, amount);
        // cringe moment
        DynamicDifficulty.sendDifficultyPointsToClient(player, false);
    }

    // Game Stages
    public static void changePlayerPointsByGlobal(EntityPlayerMP player, double amount, String stageName, boolean lost) {
        player.sendMessage(new TextComponentString("first method: " + amount + " points"));
        changePlayerPointsByGlobal(player, amount, stageName, lost, false);
    }
    public static void changePlayerPointsByGlobal(EntityPlayerMP player, double amount, String stageName, boolean lost, boolean spoiler) {
        player.sendMessage(new TextComponentString("second method: " + amount + " points"));
        String message;
        if (!spoiler) {
            if (!lost) {
                message = "gs1";
            } else {
                message = "gs2";
            }
            player.sendMessage(new TextComponentString(CustomTextManager.getText(message, amount, stageName)));
        } else {
            if (!lost) {
                message = "gs_spoiler1";
            } else {
                message = "gs_spoiler2";
            }
            player.sendMessage(new TextComponentString(CustomTextManager.getText(message, amount)));
        }

        universalChanger(player, amount);
    }

    // SRP evolution phases
    public static void changePlayerPointsByGlobal(EntityPlayerMP player, double amount, byte phase, byte previousPhase) {
        if (previousPhase > phase) {
            player.sendMessage(new TextComponentString(CustomTextManager.getText("srp2", amount, phase, previousPhase)));
            universalChanger(player, amount);
        } else {
            player.sendMessage(new TextComponentString(CustomTextManager.getText("srp1", amount, phase)));
            universalChanger(player, amount);
        }
    }

    // Sleep through night
    public static void changePlayerPointsByGlobal(EntityPlayerMP player, double amount, int sleepNumber, double amountForNextSleep, boolean shouldShowNextSleep) {
        universalChanger(player, amount);
        if (shouldShowNextSleep) {
            player.sendMessage(new TextComponentString(CustomTextManager.getText("sleep1", sleepNumber, amount, amountForNextSleep)));
        } else {
            player.sendMessage(new TextComponentString(CustomTextManager.getText("sleep2", sleepNumber, amount)));
        }
    }
}