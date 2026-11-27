package dev.easierghostblock.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EGBConfigScreen {
    private EGBConfigScreen() {
    }

    private static Component t(String key) {
        return Component.translatable("easierghostblock.config." + key);
    }

    private static OptionDescription d(String key) {
        return OptionDescription.of(Component.translatable("easierghostblock.config." + key + ".desc"));
    }

    public static Screen create(Screen parent) {
        EGBConfig cfg = EGBConfig.get();
        EGBConfig def = new EGBConfig();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("easierghostblock.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(t("category.general"))
                        .option(Option.<Boolean>createBuilder()
                                .name(t("enabled"))
                                .description(d("enabled"))
                                .binding(def.enabled, () -> cfg.enabled, v -> cfg.enabled = v)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<DelayUnit>createBuilder()
                                .name(t("unit"))
                                .description(d("unit"))
                                .binding(def.unit, () -> cfg.unit, v -> cfg.unit = v)
                                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(DelayUnit.class))
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(t("group.delay"))
                                .description(d("group.delay"))
                                .option(Option.<Integer>createBuilder()
                                        .name(t("delayTicks"))
                                        .description(d("delayTicks"))
                                        .binding(def.delayTicks, () -> cfg.delayTicks, v -> cfg.delayTicks = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 40).step(1)
                                                .formatValue(v -> Component.literal(v + " ticks (" + v * 50 + " ms)")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(t("delayTicksExact"))
                                        .description(d("delayTicksExact"))
                                        .binding(def.delayTicks, () -> cfg.delayTicks, v -> cfg.delayTicks = v)
                                        .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                                .range(0, EGBConfig.MAX_TICKS))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(t("delayMilliseconds"))
                                        .description(d("delayMilliseconds"))
                                        .binding(def.delayMilliseconds, () -> cfg.delayMilliseconds, v -> cfg.delayMilliseconds = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 2000).step(5)
                                                .formatValue(v -> Component.literal(v + " ms")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(t("delayMillisecondsExact"))
                                        .description(d("delayMillisecondsExact"))
                                        .binding(def.delayMilliseconds, () -> cfg.delayMilliseconds, v -> cfg.delayMilliseconds = v)
                                        .controller(opt -> IntegerFieldControllerBuilder.create(opt)
                                                .range(0, EGBConfig.MAX_MILLISECONDS))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(t("subtractPing"))
                                        .description(d("subtractPing"))
                                        .binding(def.subtractPing, () -> cfg.subtractPing, v -> cfg.subtractPing = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .group(OptionGroup.createBuilder()
                                .name(t("group.where"))
                                .description(d("group.where"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(t("applyInSingleplayer"))
                                        .description(d("applyInSingleplayer"))
                                        .binding(def.applyInSingleplayer, () -> cfg.applyInSingleplayer, v -> cfg.applyInSingleplayer = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(t("applyInMultiplayer"))
                                        .description(d("applyInMultiplayer"))
                                        .binding(def.applyInMultiplayer, () -> cfg.applyInMultiplayer, v -> cfg.applyInMultiplayer = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .save(EGBConfig::save)
                .build()
                .generateScreen(parent);
    }
}
