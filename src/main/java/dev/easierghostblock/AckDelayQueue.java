package dev.easierghostblock;

import dev.easierghostblock.config.DelayUnit;
import dev.easierghostblock.config.EGBConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Holds back the server's block-change acknowledgements for a configurable time.
 * Everything here runs on the client (render) thread, except the timer which only
 * schedules a flush back onto that thread.
 */
public final class AckDelayQueue {
    private record Pending(ClientLevel level, int sequence, long dueNanos, long dueTick) {
    }

    private static final ArrayDeque<Pending> QUEUE = new ArrayDeque<>();
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "EasierGhostBlock-Timer");
        t.setDaemon(true);
        return t;
    });

    private static long clientTicks;
    private static boolean releasing;

    private AckDelayQueue() {
    }

    /**
     * @return true if the ack was queued and the vanilla handling must be cancelled for now.
     */
    public static boolean tryDelay(ClientLevel level, int sequence) {
        if (releasing) return false;

        EGBConfig cfg = EGBConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!cfg.enabled) return false;
        boolean singleplayer = mc.hasSingleplayerServer();
        if (singleplayer ? !cfg.applyInSingleplayer : !cfg.applyInMultiplayer) return false;

        long delayMs = cfg.unit == DelayUnit.TICKS ? cfg.delayTicks * 50L : cfg.delayMilliseconds;
        if (cfg.subtractPing) delayMs -= currentPing(mc);
        if (delayMs <= 0) return false;

        if (cfg.unit == DelayUnit.TICKS) {
            // Round to whole ticks so the value stays tick-accurate.
            long ticks = Math.max(1, Math.round(delayMs / 50.0));
            QUEUE.add(new Pending(level, sequence, Long.MIN_VALUE, clientTicks + ticks));
        } else {
            QUEUE.add(new Pending(level, sequence, System.nanoTime() + delayMs * 1_000_000L, Long.MIN_VALUE));
            // Ticks are 50 ms apart, so use a timer for millisecond precision.
            TIMER.schedule(() -> mc.execute(AckDelayQueue::flush), delayMs, TimeUnit.MILLISECONDS);
        }
        return true;
    }

    private static long currentPing(Minecraft mc) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null || mc.player == null) return 0;
        PlayerInfo info = connection.getPlayerInfo(mc.player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }

    /** Called at the end of every client tick. */
    public static void onClientTick() {
        clientTicks++;
        flush();
    }

    /** Releases every queued ack whose delay has elapsed. */
    public static void flush() {
        if (QUEUE.isEmpty()) return;
        ClientLevel current = Minecraft.getInstance().level;
        long now = System.nanoTime();
        Iterator<Pending> it = QUEUE.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (p.level != current) {
                it.remove(); // world changed or disconnected: the prediction state is gone anyway
                continue;
            }
            boolean due = p.dueTick != Long.MIN_VALUE ? clientTicks >= p.dueTick : now >= p.dueNanos;
            if (!due) continue;
            it.remove();
            release(p.level, p.sequence);
        }
    }

    /** Immediately releases everything (used when the mod is turned off). */
    public static void releaseAll() {
        ClientLevel current = Minecraft.getInstance().level;
        while (!QUEUE.isEmpty()) {
            Pending p = QUEUE.poll();
            if (p.level == current) release(p.level, p.sequence);
        }
    }

    public static void clear() {
        QUEUE.clear();
    }

    private static void release(ClientLevel level, int sequence) {
        releasing = true;
        try {
            level.handleBlockChangedAck(sequence);
        } finally {
            releasing = false;
        }
    }
}
