package dev.easierghostblock.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 26.2+: screen handling moved to Minecraft#gui. */
public final class ScreenHelper {
    private ScreenHelper() {
    }

    public static void open(Screen screen) {
        Minecraft.getInstance().gui.setScreen(screen);
    }
}
