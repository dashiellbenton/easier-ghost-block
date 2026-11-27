package dev.easierghostblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.easierghostblock.EasierGhostBlock;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Plain JSON config stored at {@code config/easierghostblock.json}.
 * Edited in-game through the YACL screen (Mod Menu or {@code /easierghostblock}).
 */
public final class EGBConfig {
    public static final int MAX_TICKS = 200;          // 10 seconds
    public static final int MAX_MILLISECONDS = 10_000; // 10 seconds

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("easierghostblock.json");

    private static EGBConfig instance = new EGBConfig();

    /** Master switch. */
    public boolean enabled = true;
    /** Which of the two delay values below is used. */
    public DelayUnit unit = DelayUnit.MILLISECONDS;
    /** Extra delay in client ticks (1 tick = 50 ms). */
    public int delayTicks = 4;
    /** Extra delay in milliseconds. */
    public int delayMilliseconds = 200;
    /**
     * When true, the configured delay is treated as the total target time and your current
     * ping to the server is subtracted from it (e.g. 200 ms target with 50 ms ping adds 150 ms).
     */
    public boolean subtractPing = false;
    /** Also apply the delay in singleplayer (integrated server) worlds. */
    public boolean applyInSingleplayer = true;
    /** Also apply the delay on multiplayer servers. */
    public boolean applyInMultiplayer = true;

    public static EGBConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                EGBConfig loaded = GSON.fromJson(reader, EGBConfig.class);
                if (loaded != null) instance = loaded;
            } catch (Exception e) {
                EasierGhostBlock.LOGGER.error("Failed to read {}, using defaults", PATH, e);
            }
        }
        instance.sanitize();
        save();
    }

    public static void save() {
        instance.sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            EasierGhostBlock.LOGGER.error("Failed to write {}", PATH, e);
        }
    }

    private void sanitize() {
        if (unit == null) unit = DelayUnit.MILLISECONDS;
        delayTicks = Math.max(0, Math.min(MAX_TICKS, delayTicks));
        delayMilliseconds = Math.max(0, Math.min(MAX_MILLISECONDS, delayMilliseconds));
    }

    /** Human readable summary, e.g. "200 ms" or "4 ticks". */
    public String describeDelay() {
        String base = unit == DelayUnit.TICKS
                ? delayTicks + (delayTicks == 1 ? " tick" : " ticks") + " (" + delayTicks * 50 + " ms)"
                : delayMilliseconds + " ms";
        return subtractPing ? base + " total, minus ping" : base + " extra";
    }
}
