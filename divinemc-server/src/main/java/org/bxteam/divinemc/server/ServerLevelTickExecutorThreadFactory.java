package org.bxteam.divinemc.server;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.util.concurrent.ThreadFactory;

public class ServerLevelTickExecutorThreadFactory implements ThreadFactory {
    private final String worldName;

    public ServerLevelTickExecutorThreadFactory(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        TickThread.ServerLevelTickThread tickThread = new TickThread.ServerLevelTickThread(runnable, "serverlevel-tick-worker [" + worldName + "]");

        if (tickThread.isDaemon()) {
            tickThread.setDaemon(false);
        }

        if (tickThread.getPriority() != 5) {
            tickThread.setPriority(5);
        }

        return tickThread;
    }
}
