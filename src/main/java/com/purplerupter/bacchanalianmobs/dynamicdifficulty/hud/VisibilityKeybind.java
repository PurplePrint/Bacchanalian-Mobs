package com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.HUDConfig.visibility;

public class VisibilityKeybind {

    // TODO
    private static final KeyBinding SETTINGS_KEY = new KeyBinding("dynamicdifficulty.key.visibility", Keyboard.KEY_O, BacchanalianMobs.MODID);
    static
    {
        ClientRegistry.registerKeyBinding(SETTINGS_KEY);
    }

    @SubscribeEvent
    public void onKeyEvent(KeyInputEvent event)
    {
        if (SETTINGS_KEY.isPressed())
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.currentScreen != null)
                return;
            visibility = !visibility;
        }
    }
}