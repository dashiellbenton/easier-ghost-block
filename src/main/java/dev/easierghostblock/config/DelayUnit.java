package dev.easierghostblock.config;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum DelayUnit implements NameableEnum {
    TICKS("easierghostblock.unit.ticks"),
    MILLISECONDS("easierghostblock.unit.milliseconds");

    private final String key;

    DelayUnit(String key) {
        this.key = key;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(key);
    }
}
