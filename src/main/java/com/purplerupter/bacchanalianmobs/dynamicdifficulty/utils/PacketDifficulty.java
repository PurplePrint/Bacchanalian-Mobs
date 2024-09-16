package com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.RenderHUD;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDifficulty implements IMessage {
    private double difficultyPoints;
    private boolean isPassive;
    private float actionAmount;

    public PacketDifficulty() {}

    public PacketDifficulty(double difficultyPoints, boolean passive, float actionAmount) {
        this.difficultyPoints = difficultyPoints;
        this.isPassive = passive;
        this.actionAmount = actionAmount;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        difficultyPoints = buf.readDouble();
        isPassive = buf.readBoolean();
        actionAmount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(difficultyPoints);
        buf.writeBoolean(isPassive);
        buf.writeFloat(actionAmount);
    }

    public static class Handler implements IMessageHandler<PacketDifficulty, IMessage> {
        @Override
        public IMessage onMessage(PacketDifficulty message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                RenderHUD.INSTANCE.setDifficultyPoints(message.difficultyPoints, message.isPassive, message.actionAmount);
            });
            return null;
        }
    }
}
