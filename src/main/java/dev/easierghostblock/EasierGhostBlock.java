package dev.easierghostblock;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.easierghostblock.config.DelayUnit;
import dev.easierghostblock.config.EGBConfig;
import dev.easierghostblock.config.EGBConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasierGhostBlock implements ClientModInitializer {
    public static final String MOD_ID = "easierghostblock";
    public static final Logger LOGGER = LoggerFactory.getLogger("Easier Ghost Block");

    private static boolean openConfigNextTick;

    @Override
    public void onInitializeClient() {
        EGBConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AckDelayQueue.onClientTick();
            if (openConfigNextTick) {
                openConfigNextTick = false;
                client.setScreen(EGBConfigScreen.create(client.screen));
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> AckDelayQueue.clear());

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (String name : new String[]{"easierghostblock", "egb"}) {
                dispatcher.register(ClientCommandManager.literal(name)
                        .executes(ctx -> {
                            // The chat screen closes after the command runs, so open on the next tick.
                            openConfigNextTick = true;
                            return 1;
                        })
                        .then(ClientCommandManager.literal("status").executes(ctx -> {
                            status(ctx.getSource());
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("toggle").executes(ctx -> {
                            EGBConfig cfg = EGBConfig.get();
                            cfg.enabled = !cfg.enabled;
                            if (!cfg.enabled) AckDelayQueue.releaseAll();
                            EGBConfig.save();
                            status(ctx.getSource());
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("ticks")
                                .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(0, EGBConfig.MAX_TICKS))
                                        .executes(ctx -> {
                                            EGBConfig cfg = EGBConfig.get();
                                            cfg.unit = DelayUnit.TICKS;
                                            cfg.delayTicks = IntegerArgumentType.getInteger(ctx, "ticks");
                                            EGBConfig.save();
                                            status(ctx.getSource());
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("ms")
                                .then(ClientCommandManager.argument("milliseconds", IntegerArgumentType.integer(0, EGBConfig.MAX_MILLISECONDS))
                                        .executes(ctx -> {
                                            EGBConfig cfg = EGBConfig.get();
                                            cfg.unit = DelayUnit.MILLISECONDS;
                                            cfg.delayMilliseconds = IntegerArgumentType.getInteger(ctx, "milliseconds");
                                            EGBConfig.save();
                                            status(ctx.getSource());
                                            return 1;
                                        }))));
            }
        });
    }

    private static void status(FabricClientCommandSource source) {
        EGBConfig cfg = EGBConfig.get();
        source.sendFeedback(Component.literal("[Easier Ghost Block] ")
                .append(Component.literal(cfg.enabled ? "ON" : "OFF"))
                .append(Component.literal(" - delay: " + cfg.describeDelay())));
    }
}
