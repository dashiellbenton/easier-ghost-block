package dev.easierghostblock.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 1.21.x - 26.1: Minecraft#setScreen. */
public final class ScreenHelper {
    private ScreenHelper() {
    }

    public static void open(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
}
