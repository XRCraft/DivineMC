package org.bxteam.divinemc;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bxteam.divinemc.server.chunk.ChunkSystemAlgorithms;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("unused")
@NullMarked
public final class DivineConfig { // TODO: Remake config system
    private DivineConfig() {
        throw new IllegalStateException("Utility class");
    }

    private static final String HEADER = "This is the main configuration file for DivineMC.\n"
        + "If you need help with the configuration or have any questions related to DivineMC,\n"
        + "join us in our Discord server.\n"
        + "\n"
        + "Discord: https://discord.gg/p7cxhw7E2M \n"
        + "Docs: https://bxteam.org/docs/divinemc \n"
        + "New builds: https://github.com/BX-Team/DivineMC/releases/latest";
    private static File configFile;
    public static YamlConfiguration config;

    private static Map<String, Command> commands;

    public static int version;
    static boolean verbose;

    public static void init(File configFile) {
        DivineConfig.configFile = configFile;
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException ignored) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load divinemc.yml, please correct your syntax errors", ex);
            throw Throwables.propagate(ex);
        }
        config.options().header(HEADER);
        config.options().copyDefaults(true);
        verbose = getBoolean(config, "verbose", false);

        version = getInt(config, "config-version", 5);
        set(config, "config-version", 5);

        readConfig(DivineConfig.class, null);
    }

    public static void log(String s) {
        if (verbose) {
            log(Level.INFO, s);
        }
    }

    public static void log(Level level, String s) {
        Bukkit.getLogger().log(level, s);
    }

    static void readConfig(Class<?> clazz, @Nullable Object instance) {
        readConfig(configFile, config, clazz, instance);
    }

    public static void readConfig(File configFile, YamlConfiguration config, Class<?> clazz, @Nullable Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isPrivate(method.getModifiers())) continue;
            if (method.getParameterTypes().length != 0) continue;
            if (method.getReturnType() != Void.TYPE) continue;

            try {
                method.setAccessible(true);
                method.invoke(instance);
            } catch (InvocationTargetException ex) {
                throw Throwables.propagate(ex.getCause());
            } catch (Exception ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Error invoking " + method, ex);
            }
        }

        try {
            config.save(configFile);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save " + configFile, ex);
        }
    }

    private static void set(YamlConfiguration config, String path, Object val, String... comments) {
        config.addDefault(path, val);
        config.set(path, val);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
    }

    private static String getString(YamlConfiguration config, String path, String def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return config.getString(path, config.getString(path));
    }

    private static boolean getBoolean(YamlConfiguration config, String path, boolean def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return config.getBoolean(path, config.getBoolean(path));
    }

    private static double getDouble(YamlConfiguration config, String path, double def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return config.getDouble(path, config.getDouble(path));
    }

    private static int getInt(YamlConfiguration config, String path, int def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return config.getInt(path, config.getInt(path));
    }

    private static long getLong(YamlConfiguration config, String path, long def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return config.getLong(path, config.getLong(path));
    }

    private static <T> List<T> getList(YamlConfiguration config, String path, List<T> def, String... comments) {
        config.addDefault(path, def);
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return (List<T>) config.getList(path, def);
    }

    static Map<String, Object> getMap(YamlConfiguration config, String path, Map<String, Object> def, String... comments) {
        if (def != null && config.getConfigurationSection(path) == null) {
            config.addDefault(path, def);
            return def;
        }
        if (comments.length > 0) {
            config.setComments(path, List.of(comments));
        }
        return toMap(config.getConfigurationSection(path));
    }

    private static Map<String, Object> toMap(ConfigurationSection section) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object obj = section.get(key);
                if (obj != null) {
                    builder.put(key, obj instanceof ConfigurationSection val ? toMap(val) : obj);
                }
            }
        }
        return builder.build();
    }

    public static int parallelThreadCount = 4;
    public static boolean logContainerCreationStacktraces = false;
    private static void parallelWorldTicking() {
        parallelThreadCount = getInt(config, "settings.parallel-world-ticking.thread-count", parallelThreadCount);
        logContainerCreationStacktraces = getBoolean(config, "settings.parallel-world-ticking.log-container-creation-stacktraces", logContainerCreationStacktraces);
    }

    public static boolean nativeAccelerationEnabled = true;
    public static boolean allowAVX512 = false;
    public static int isaTargetLevelOverride = -1;
    public static long chunkDataCacheSoftLimit = 8192L;
    public static long chunkDataCacheLimit = 32678L;
    public static int maxViewDistance = 32;
    public static ChunkSystemAlgorithms chunkWorkerAlgorithm = ChunkSystemAlgorithms.C2ME;
    public static int threadPoolPriority = Thread.NORM_PRIORITY + 1;
    public static boolean enableSecureSeed = false;
    public static boolean enableDensityFunctionCompiler = false;
    public static boolean enableStructureLayoutOptimizer = true;
    public static boolean deduplicateShuffledTemplatePoolElementList = false;

    private static void chunkGeneration() {
        nativeAccelerationEnabled = getBoolean(config, "settings.chunk-generation.native-acceleration-enabled", nativeAccelerationEnabled);

        allowAVX512 = getBoolean(config, "settings.chunk-generation.allow-avx512", allowAVX512,
            "Enables AVX512 support for natives-math optimizations");
        isaTargetLevelOverride = getInt(config, "settings.chunk-generation.isa-target-level-override", isaTargetLevelOverride,
            "Overrides the ISA target located by the native loader, which allows forcing AVX512 (must be a value between 6-9 for AVX512 support).",
            "Value must be between 1-9, and -1 to disable override");

        if (isaTargetLevelOverride < -1 || isaTargetLevelOverride > 9) {
            log(Level.WARNING, "Invalid ISA target level override: " + isaTargetLevelOverride + ", resetting to -1");
            isaTargetLevelOverride = -1;
        }

        chunkDataCacheSoftLimit = getLong(config, "settings.chunk-generation.chunk-data-cache-soft-limit", chunkDataCacheSoftLimit);
        chunkDataCacheLimit = getLong(config, "settings.chunk-generation.chunk-data-cache-limit", chunkDataCacheLimit);
        maxViewDistance = getInt(config, "settings.chunk-generation.max-view-distance", maxViewDistance,
            "Changes the maximum view distance for the server, allowing clients to have render distances higher than 32");

        chunkWorkerAlgorithm = ChunkSystemAlgorithms.valueOf(getString(config, "settings.chunk-generation.chunk-worker-algorithm", chunkWorkerAlgorithm.name(),
            "Modifies what algorithm the chunk system will use to define thread counts. values: MOONRISE, C2ME, C2ME_AGGRESSIVE"));
        threadPoolPriority = getInt(config, "settings.chunk-generation.thread-pool-priority", threadPoolPriority,
            "Sets the priority of the thread pool used for chunk generation");

        enableSecureSeed = getBoolean(config, "settings.misc.enable-secure-seed", enableSecureSeed,
            "This feature is based on Secure Seed mod by Earthcomputer.",
            "",
            "Terrain and biome generation remains the same, but all the ores and structures are generated with 1024-bit seed, instead of the usual 64-bit seed.",
            "This seed is almost impossible to crack, and there are no weird links between structures.");

        enableDensityFunctionCompiler = getBoolean(config, "settings.chunk-generation.experimental.enable-density-function-compiler", enableDensityFunctionCompiler,
            "Whether to use density function compiler to accelerate world generation",
            "",
            "Density function: https://minecraft.wiki/w/Density_function",
            "",
            "This functionality compiles density functions from world generation",
            "datapacks (including vanilla generation) to JVM bytecode to increase",
            "performance by allowing JVM JIT to better optimize the code.",
            "All functions provided by vanilla are implemented.",
            "",
            "Please test if this optimization actually benefits your server, as",
            "it can sometimes slow down chunk performance than speed it up.");

        enableStructureLayoutOptimizer = getBoolean(config, "settings.chunk-generation.experimental.enable-structure-layout-optimizer", enableStructureLayoutOptimizer,
            "Enables a port of the mod StructureLayoutOptimizer, which optimizes general Jigsaw structure generation");
        deduplicateShuffledTemplatePoolElementList = getBoolean(config, "settings.chunk-generation.experimental.deduplicate-shuffled-template-pool-element-list", deduplicateShuffledTemplatePoolElementList,
            "Whether to use an alternative strategy to make structure layouts generate slightly even faster than",
            "the default optimization this mod has for template pool weights. This alternative strategy works by",
            "changing the list of pieces that structures collect from the template pool to not have duplicate entries.",
            "",
            "This will not break the structure generation, but it will make the structure layout different than",
            "if this config was off (breaking vanilla seed parity). The cost of speed may be worth it in large",
            "modpacks where many structure mods are using very high weight values in their template pools.");
    }

    public static boolean skipUselessSecondaryPoiSensor = true;
    public static boolean clumpOrbs = true;
    public static boolean ignoreMovedTooQuicklyWhenLagging = true;
    public static boolean alwaysAllowWeirdMovement = true;
    public static boolean updateSuppressionCrashFix = true;

    private static void miscSettings() {
        skipUselessSecondaryPoiSensor = getBoolean(config, "settings.misc.skip-useless-secondary-poi-sensor", skipUselessSecondaryPoiSensor);
        clumpOrbs = getBoolean(config, "settings.misc.clump-orbs", clumpOrbs,
            "Clumps experience orbs together to reduce entity count");
        ignoreMovedTooQuicklyWhenLagging = getBoolean(config, "settings.misc.ignore-moved-too-quickly-when-lagging", ignoreMovedTooQuicklyWhenLagging,
            "Improves general gameplay experience of the player when the server is lagging, as they won't get lagged back (message 'moved too quickly')");
        alwaysAllowWeirdMovement = getBoolean(config, "settings.misc.always-allow-weird-movement", alwaysAllowWeirdMovement,
            "Means ignoring messages like 'moved too quickly' and 'moved wrongly'");
        updateSuppressionCrashFix = getBoolean(config, "settings.misc.update-suppression-crash-fix", updateSuppressionCrashFix);
    }

    public static boolean enableFasterTntOptimization = true;
    public static boolean explosionNoBlockDamage = false;
    public static double tntRandomRange = -1;

    private static void tntOptimization() {
        enableFasterTntOptimization = getBoolean(config, "settings.tnt-optimization.enable-faster-tnt-optimization", enableFasterTntOptimization);
        explosionNoBlockDamage = getBoolean(config, "settings.tnt-optimization.explosion-no-block-damage", explosionNoBlockDamage);
        tntRandomRange = getDouble(config, "settings.tnt-optimization.tnt-random-range", tntRandomRange);
    }

    public static boolean lagCompensationEnabled = true;
    public static boolean blockEntityAcceleration = false;
    public static boolean blockBreakingAcceleration = true;
    public static boolean eatingAcceleration = true;
    public static boolean potionEffectAcceleration = true;
    public static boolean fluidAcceleration = true;
    public static boolean pickupAcceleration = true;
    public static boolean portalAcceleration = true;
    public static boolean timeAcceleration = true;
    public static boolean randomTickSpeedAcceleration = true;

    private static void lagCompensation() {
        lagCompensationEnabled = getBoolean(config, "settings.lag-compensation.enabled", lagCompensationEnabled, "Improves the player experience when TPS is low");
        blockEntityAcceleration = getBoolean(config, "settings.lag-compensation.block-entity-acceleration", blockEntityAcceleration);
        blockBreakingAcceleration = getBoolean(config, "settings.lag-compensation.block-breaking-acceleration", blockBreakingAcceleration);
        eatingAcceleration = getBoolean(config, "settings.lag-compensation.eating-acceleration", eatingAcceleration);
        potionEffectAcceleration = getBoolean(config, "settings.lag-compensation.potion-effect-acceleration", potionEffectAcceleration);
        fluidAcceleration = getBoolean(config, "settings.lag-compensation.fluid-acceleration", fluidAcceleration);
        pickupAcceleration = getBoolean(config, "settings.lag-compensation.pickup-acceleration", pickupAcceleration);
        portalAcceleration = getBoolean(config, "settings.lag-compensation.portal-acceleration", portalAcceleration);
        timeAcceleration = getBoolean(config, "settings.lag-compensation.time-acceleration", timeAcceleration);
        randomTickSpeedAcceleration = getBoolean(config, "settings.lag-compensation.random-tick-speed-acceleration", randomTickSpeedAcceleration);
    }

    public static boolean noChatReportsEnabled = false;
    public static boolean noChatReportsAddQueryData = true;
    public static boolean noChatReportsConvertToGameMessage = true;
    public static boolean noChatReportsDebugLog = false;
    public static boolean noChatReportsDemandOnClient = false;
    public static String noChatReportsDisconnectDemandOnClientMessage = "You do not have No Chat Reports, and this server is configured to require it on client!";

    private static void noChatReports() {
        noChatReportsEnabled = getBoolean(config, "settings.no-chat-reports.enabled", noChatReportsEnabled,
            "Enables or disables the No Chat Reports feature");
        noChatReportsAddQueryData = getBoolean(config, "settings.no-chat-reports.add-query-data", noChatReportsAddQueryData,
            "Should server include extra query data to help clients know that your server is secure");
        noChatReportsConvertToGameMessage = getBoolean(config, "settings.no-chat-reports.convert-to-game-message", noChatReportsConvertToGameMessage,
            "Should the server convert all player messages to system messages");
        noChatReportsDebugLog = getBoolean(config, "settings.no-chat-reports.debug-log", noChatReportsDebugLog);
        noChatReportsDemandOnClient = getBoolean(config, "settings.no-chat-reports.demand-on-client", noChatReportsDemandOnClient,
            "Should the server require No Chat Reports on the client side");
        noChatReportsDisconnectDemandOnClientMessage = getString(config, "settings.no-chat-reports.disconnect-demand-on-client-message", noChatReportsDisconnectDemandOnClientMessage,
            "Message to send to the client when they are disconnected for not having No Chat Reports");
    }

    public static boolean asyncPathfinding = true;
    public static int asyncPathfindingMaxThreads = 2;
    public static int asyncPathfindingKeepalive = 60;

    private static void asyncPathfinding() {
        asyncPathfinding = getBoolean(config, "settings.async-pathfinding.enable", asyncPathfinding);
        asyncPathfindingMaxThreads = getInt(config, "settings.async-pathfinding.max-threads", asyncPathfindingMaxThreads);
        asyncPathfindingKeepalive = getInt(config, "settings.async-pathfinding.keepalive", asyncPathfindingKeepalive);

        if (asyncPathfindingMaxThreads < 0) {
            asyncPathfindingMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() + asyncPathfindingMaxThreads, 1);
        } else if (asyncPathfindingMaxThreads == 0) {
            asyncPathfindingMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
        }

        if (!asyncPathfinding) {
            asyncPathfindingMaxThreads = 0;
        } else {
            Bukkit.getLogger().log(Level.INFO, "Using " + asyncPathfindingMaxThreads + " threads for Async Pathfinding");
        }
    }

    public static boolean multithreadedEnabled = true;
    public static boolean multithreadedCompatModeEnabled = false;
    public static int asyncEntityTrackerMaxThreads = 1;
    public static int asyncEntityTrackerKeepalive = 60;

    private static void multithreadedTracker() {
        multithreadedEnabled = getBoolean(config, "settings.multithreaded-tracker.enable", multithreadedEnabled);
        multithreadedCompatModeEnabled = getBoolean(config, "settings.multithreaded-tracker.compat-mode", multithreadedCompatModeEnabled);
        asyncEntityTrackerMaxThreads = getInt(config, "settings.multithreaded-tracker.max-threads", asyncEntityTrackerMaxThreads);
        asyncEntityTrackerKeepalive = getInt(config, "settings.multithreaded-tracker.keepalive", asyncEntityTrackerKeepalive);

        if (asyncEntityTrackerMaxThreads < 0) {
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() + asyncEntityTrackerMaxThreads, 1);
        } else if (asyncEntityTrackerMaxThreads == 0) {
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
        }

        if (!multithreadedEnabled) {
            asyncEntityTrackerMaxThreads = 0;
        } else {
            Bukkit.getLogger().log(Level.INFO, "Using " + asyncEntityTrackerMaxThreads + " threads for Async Entity Tracker");
        }
    }
}
