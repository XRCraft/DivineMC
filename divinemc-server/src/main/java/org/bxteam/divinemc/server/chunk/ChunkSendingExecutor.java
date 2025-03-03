package org.bxteam.divinemc.server.chunk;

import ca.spottedleaf.moonrise.common.util.TickThread;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bxteam.divinemc.DivineConfig;
import org.bxteam.divinemc.util.NamedAgnosticThreadFactory;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkSendingExecutor {
    private static final ExecutorService SERVICE = DivineConfig.asyncChunkSendingEnabled
        ? DivineConfig.asyncChunkSendingUseVirtualThreads ?
            Executors.newVirtualThreadPerTaskExecutor() :
            Executors.newFixedThreadPool(
                DivineConfig.asyncChunkSendingThreadCount,
                new NamedAgnosticThreadFactory<>("chunk_sending", TickThread::new, Thread.NORM_PRIORITY)
            )
        : null;

    public static void execute(Runnable runnable, ServerLevel level) {
        runnable = wrapRunnable(runnable, level);
        if (DivineConfig.asyncChunkSendingEnabled) {
            SERVICE.submit(runnable);
        } else {
            runnable.run();
        }
    }

    private static @NotNull Runnable wrapRunnable(Runnable runnable, final ServerLevel level) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                MinecraftServer.LOGGER.warn("Failed to send chunk data! Retrying...");
                level.getServer().scheduleOnMain(() -> {
                    try {
                        runnable.run();
                    } catch (Throwable failed) {
                        MinecraftServer.LOGGER.error("Failed to send chunk data! (2nd attempt). Logging error log", failed);
                    }
                });
            }
        };
    }
}
