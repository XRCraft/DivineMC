package org.bxteam.divinemc;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bxteam.divinemc.entity.pathfinding.PathfindTaskRejectPolicy;
import org.bxteam.divinemc.server.chunk.ChunkSystemAlgorithms;
import org.bxteam.divinemc.server.chunk.ChunkTaskPriority;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.comments.CommentType;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings({"unused", "SameParameterValue"})
public class DivineConfig {
    private static final String HEADER = """
        This is the main configuration file for DivineMC.
        If you need help with the configuration or have any questions related to DivineMC,
        join us in our Discord server.

        Discord: https://discord.gg/p7cxhw7E2M
        Docs: https://bxteam.org/docs/divinemc
        Downloads: https://github.com/BX-Team/DivineMC/releases""";

    public static final Logger LOGGER = LogManager.getLogger(DivineConfig.class.getSimpleName());
    public static final int CONFIG_VERSION = 5;

    private static File configFile;
    public static final YamlFile config = new YamlFile();

	private static ConfigurationSection convertToBukkit(org.simpleyaml.configuration.ConfigurationSection section) {
		ConfigurationSection newSection = new MemoryConfiguration();
		for (String key : section.getKeys(false)) {
			if (section.isConfigurationSection(key)) {
				newSection.set(key, convertToBukkit(section.getConfigurationSection(key)));
			} else {
				newSection.set(key, section.get(key));
			}
		}
		return newSection;
	}

	public static ConfigurationSection getConfigCopy() {
		return convertToBukkit(config);
	}

	public static void init(File configFile) throws IOException {
        DivineConfig.configFile = configFile;
		if (configFile.exists()) {
			try {
				config.load(configFile);
			} catch (InvalidConfigurationException e) {
				throw new IOException(e);
			}
		}

		getInt("version", CONFIG_VERSION);
        config.options().header(HEADER);

        readConfig(DivineConfig.class, null);
	}

    static void readConfig(Class<?> clazz, Object instance) throws IOException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                        LOGGER.error("Error invoking {}", method, ex);
                    }
                }
            }
        }

        config.save(configFile);
    }

	private static void setComment(String key, String... comment) {
		if (config.contains(key)) {
			config.setComment(key, String.join("\n", comment), CommentType.BLOCK);
		}
	}

    private static void ensureDefault(String key, Object defaultValue, String... comment) {
        if (!config.contains(key)) config.set(key, defaultValue);
        if (comment.length > 0) config.setComment(key, String.join("\n", comment), CommentType.BLOCK);
    }

	private static boolean getBoolean(String key, boolean defaultValue, String... comment) {
		return getBoolean(key, null, defaultValue, comment);
	}

	private static boolean getBoolean(String key, @Nullable String oldKey, boolean defaultValue, String... comment) {
		ensureDefault(key, defaultValue, comment);
		return config.getBoolean(key, defaultValue);
	}

	private static int getInt(String key, int defaultValue, String... comment) {
		return getInt(key, null, defaultValue, comment);
	}

	private static int getInt(String key, @Nullable String oldKey, int defaultValue, String... comment) {
		ensureDefault(key, defaultValue, comment);
		return config.getInt(key, defaultValue);
	}

	private static double getDouble(String key, double defaultValue, String... comment) {
		return getDouble(key, null, defaultValue, comment);
	}

	private static double getDouble(String key, @Nullable String oldKey, double defaultValue, String... comment) {
		ensureDefault(key, defaultValue, comment);
		return config.getDouble(key, defaultValue);
	}

    private static long getLong(String key, long defaultValue, String... comment) {
        return getLong(key, null, defaultValue, comment);
    }

    private static long getLong(String key, @Nullable String oldKey, long defaultValue, String... comment) {
        ensureDefault(key, defaultValue, comment);
        return config.getLong(key, defaultValue);
    }

	private static String getString(String key, String defaultValue, String... comment) {
		return getOldString(key, null, defaultValue, comment);
	}

	private static String getOldString(String key, @Nullable String oldKey, String defaultValue, String... comment) {
		ensureDefault(key, defaultValue, comment);
		return config.getString(key, defaultValue);
	}

	private static List<String> getStringList(String key, List<String> defaultValue, String... comment) {
		return getStringList(key, null, defaultValue, comment);
	}

	private static List<String> getStringList(String key, @Nullable String oldKey, List<String> defaultValue, String... comment) {
		ensureDefault(key, defaultValue, comment);
		return config.getStringList(key);
	}

    public static boolean enableParallelWorldTicking = true;
    public static int parallelThreadCount = 4;
    public static boolean logContainerCreationStacktraces = false;
    public static boolean disableHardThrow = false;
    private static void parallelWorldTicking() {
        enableParallelWorldTicking = getBoolean("settings.parallel-world-ticking.enable", enableParallelWorldTicking,
            "Enables Parallel World Ticking, which executes each world’s tick in a separate thread while ensuring that all worlds complete their tick before the next cycle begins.",
            "",
            "Read more info about this feature at https://bxteam.org/docs/divinemc/features/parallel-world-ticking");
        parallelThreadCount = getInt("settings.parallel-world-ticking.thread-count", parallelThreadCount);
        logContainerCreationStacktraces = getBoolean("settings.parallel-world-ticking.log-container-creation-stacktraces", logContainerCreationStacktraces);
        disableHardThrow = getBoolean("settings.parallel-world-ticking.disable-hard-throw", disableHardThrow,
            "Disables annoying 'not on main thread' throws. But, THIS IS NOT RECOMMENDED because you SHOULD FIX THE ISSUES THEMSELVES instead of RISKING DATA CORRUPTION! If you lose something, take the blame on yourself.");
    }

    public static boolean nativeAccelerationEnabled = true;
    public static boolean allowAVX512 = false;
    public static int isaTargetLevelOverride = -1;
    public static long chunkDataCacheSoftLimit = 8192L;
    public static long chunkDataCacheLimit = 32678L;
    public static int maxViewDistance = 32;
    public static int playerNearChunkDetectionRange = 128;
    public static ChunkSystemAlgorithms chunkWorkerAlgorithm = ChunkSystemAlgorithms.C2ME;
    public static ChunkTaskPriority chunkTaskPriority = ChunkTaskPriority.EUCLIDEAN_CIRCLE_PATTERN;
    public static int threadPoolPriority = Thread.NORM_PRIORITY + 1;
    public static boolean enableAsyncNoiseFill = false;
    public static boolean enableSecureSeed = false;
    public static boolean smoothBedrockLayer = false;
    public static boolean slopesVisualFix = false;
    public static boolean enableStructureLayoutOptimizer = true;
    public static boolean deduplicateShuffledTemplatePoolElementList = false;
    private static void chunkGeneration() {
        nativeAccelerationEnabled = getBoolean("settings.chunk-generation.native-acceleration-enabled", nativeAccelerationEnabled);

        allowAVX512 = getBoolean("settings.chunk-generation.allow-avx512", allowAVX512,
            "Enables AVX512 support for natives-math optimizations");
        isaTargetLevelOverride = getInt("settings.chunk-generation.isa-target-level-override", isaTargetLevelOverride,
            "Overrides the ISA target located by the native loader, which allows forcing AVX512 (must be a value between 6-9 for AVX512 support).",
            "Value must be between 1-9, and -1 to disable override");

        if (isaTargetLevelOverride < -1 || isaTargetLevelOverride > 9) {
            LOGGER.warn("Invalid ISA target level override: {}, resetting to -1", isaTargetLevelOverride);
            isaTargetLevelOverride = -1;
        }

        chunkDataCacheSoftLimit = getLong("settings.chunk-generation.chunk-data-cache-soft-limit", chunkDataCacheSoftLimit);
        chunkDataCacheLimit = getLong("settings.chunk-generation.chunk-data-cache-limit", chunkDataCacheLimit);
        maxViewDistance = getInt("settings.chunk-generation.max-view-distance", maxViewDistance,
            "Changes the maximum view distance for the server, allowing clients to have render distances higher than 32");
        playerNearChunkDetectionRange = getInt("settings.chunk-generation.player-near-chunk-detection-range", playerNearChunkDetectionRange,
            "In certain checks, like if a player is near a chunk(primarily used for spawning), it checks if the player is within a certain",
            "circular range of the chunk. This configuration allows configurability of the distance(in blocks) the player must be to pass the check.",
            "",
            "This value is used in the calculation 'range/16' to get the distance in chunks any player must be to allow the check to pass.",
            "By default, this range is computed to 8, meaning a player must be within an 8 chunk radius of a chunk position to pass.",
            "Keep in mind the result is rounded to the nearest whole number.");

        if (playerNearChunkDetectionRange < 0) {
            LOGGER.warn("Invalid player near chunk detection range: {}, resetting to default (128)", playerNearChunkDetectionRange);
            playerNearChunkDetectionRange = 128;
        }

        chunkWorkerAlgorithm = ChunkSystemAlgorithms.valueOf(getString("settings.chunk-generation.chunk-worker-algorithm", chunkWorkerAlgorithm.name(),
            "Modifies what algorithm the chunk system will use to define thread counts.",
            "Valid values:",
            " - MOONRISE: Default algorithm, used by default in Paper",
            " - C2ME: Algorithm used by C2ME (old)",
            " - C2ME_NEW: Modern algorithm used by C2ME"));
        chunkTaskPriority = ChunkTaskPriority.valueOf(getString("settings.chunk-generation.chunk-task-priority", chunkTaskPriority.name(),
            "Sets the algorithm for determining chunk task priorities (generation, loading and etc.).",
            "Valid values:",
            " - EUCLIDEAN_CIRCLE_PATTERN: Euclidean distance squared algorithm, chunk priorities will be ordered in a circle pattern",
            " - DEFAULT_DIAMOND_PATTERN: Default one, chunk priorities will be ordered in a diamond pattern"));
        threadPoolPriority = getInt("settings.chunk-generation.thread-pool-priority", threadPoolPriority,
            "Sets the priority of the thread pool used for chunk generation");
        enableAsyncNoiseFill = getBoolean("settings.chunk-generation.enable-async-noise-fill", enableAsyncNoiseFill,
            "Runs noise filling and biome populating in a virtual thread executor. If disabled, it will run sync.");

        enableSecureSeed = getBoolean("settings.chunk-generation.enable-secure-seed", enableSecureSeed,
            "This feature is based on Secure Seed mod by Earthcomputer.",
            "",
            "Terrain and biome generation remains the same, but all the ores and structures are generated with 1024-bit seed, instead of the usual 64-bit seed.",
            "This seed is almost impossible to crack, and there are no weird links between structures.");

        smoothBedrockLayer = getBoolean("settings.chunk-generation.smooth-bedrock-layer", smoothBedrockLayer,
            "Smoothens the bedrock layer at the bottom of overworld, and on the top of nether during the world generation.");
        slopesVisualFix = getBoolean("settings.chunk-generation.slopes-visual-fix", slopesVisualFix,
            "Fixes MC-258859, fixing slopes visual bug in biomes like Snowy Slopes, Frozen Peaks, Jagged Peaks, and including Terralith.");

        enableStructureLayoutOptimizer = getBoolean("settings.chunk-generation.experimental.enable-structure-layout-optimizer", enableStructureLayoutOptimizer,
            "Enables a port of the mod StructureLayoutOptimizer, which optimizes general Jigsaw structure generation");
        deduplicateShuffledTemplatePoolElementList = getBoolean("settings.chunk-generation.experimental.deduplicate-shuffled-template-pool-element-list", deduplicateShuffledTemplatePoolElementList,
            "Whether to use an alternative strategy to make structure layouts generate slightly even faster than",
            "the default optimization this mod has for template pool weights. This alternative strategy works by",
            "changing the list of pieces that structures collect from the template pool to not have duplicate entries.",
            "",
            "This will not break the structure generation, but it will make the structure layout different than",
            "if this config was off (breaking vanilla seed parity). The cost of speed may be worth it in large",
            "modpacks where many structure mods are using very high weight values in their template pools.");
    }

    public static boolean asyncChunkSendingEnabled = true;
    public static int asyncChunkSendingThreadCount = 1;
    public static boolean asyncChunkSendingUseVirtualThreads = false;
    private static void asyncChunkSending() {
        asyncChunkSendingEnabled = getBoolean("settings.async-chunk-sending.enable", asyncChunkSendingEnabled,
            "Enables chunk sending runs off-main thread.");

        asyncChunkSendingThreadCount = getInt("settings.async-chunk-sending.thread-count", asyncChunkSendingThreadCount,
            "Amount of threads to use for async chunk sending. (If use-virtual-threads is enabled, this will be ignored)");

        asyncChunkSendingUseVirtualThreads = getBoolean("settings.async-chunk-sending.use-virtual-threads", asyncChunkSendingUseVirtualThreads,
            "Similar to the 'virtual-thread' options. This will use the virtual thread executor for chunk sending.");
    }

    public static boolean enableRegionizedChunkTicking = false;
    public static int regionizedChunkTickingExecutorThreadCount = 4;
    public static int regionizedChunkTickingExecutorThreadPriority = Thread.NORM_PRIORITY + 2;
    private static void regionizedChunkTicking() {
        enableRegionizedChunkTicking = getBoolean("settings.regionized-chunk-ticking.enable", enableRegionizedChunkTicking,
            "Enables regionized chunk ticking, similar to like Folia works.",
            "",
            "Read more info about this feature at https://bxteam.org/docs/divinemc/features/regionized-chunk-ticking");

        regionizedChunkTickingExecutorThreadCount = getInt("settings.regionized-chunk-ticking.executor-thread-count", regionizedChunkTickingExecutorThreadCount,
            "The amount of threads to allocate to regionized chunk ticking.");
        regionizedChunkTickingExecutorThreadPriority = getInt("settings.regionized-chunk-ticking.executor-thread-priority", regionizedChunkTickingExecutorThreadPriority,
            "Configures the thread priority of the executor");

        if (regionizedChunkTickingExecutorThreadCount < 1 || regionizedChunkTickingExecutorThreadCount > 10) {
            LOGGER.warn("Invalid regionized chunk ticking thread count: {}, resetting to default (5)", regionizedChunkTickingExecutorThreadCount);
            regionizedChunkTickingExecutorThreadCount = 5;
        }
    }

    public static boolean skipUselessSecondaryPoiSensor = true;
    public static boolean clumpOrbs = true;
    public static boolean ignoreMovedTooQuicklyWhenLagging = true;
    public static boolean alwaysAllowWeirdMovement = true;
    public static boolean updateSuppressionCrashFix = true;
    public static boolean useCompactBitStorage = false;
    public static boolean fixIncorrectBounceLogic = false;
    public static boolean forceMinecraftCommand = false;
    public static boolean disableLeafDecay = false;
    public static boolean commandBlockParseResultsCaching = true;
    private static void miscSettings() {
        skipUselessSecondaryPoiSensor = getBoolean("settings.misc.skip-useless-secondary-poi-sensor", skipUselessSecondaryPoiSensor);
        clumpOrbs = getBoolean("settings.misc.clump-orbs", clumpOrbs,
            "Clumps experience orbs together to reduce entity count");
        ignoreMovedTooQuicklyWhenLagging = getBoolean("settings.misc.ignore-moved-too-quickly-when-lagging", ignoreMovedTooQuicklyWhenLagging,
            "Improves general gameplay experience of the player when the server is lagging, as they won't get lagged back (message 'moved too quickly')");
        alwaysAllowWeirdMovement = getBoolean("settings.misc.always-allow-weird-movement", alwaysAllowWeirdMovement,
            "Means ignoring messages like 'moved too quickly' and 'moved wrongly'");
        updateSuppressionCrashFix = getBoolean("settings.misc.update-suppression-crash-fix", updateSuppressionCrashFix);
        useCompactBitStorage = getBoolean("settings.misc.use-compact-bit-storage", useCompactBitStorage,
            "Fixes memory waste caused by sending empty chunks as if they contain blocks. Can significantly reduce memory usage.");
        fixIncorrectBounceLogic = getBoolean("settings.misc.fix-incorrect-bounce-logic", fixIncorrectBounceLogic,
            "Fixes incorrect bounce logic in SlimeBlock.");
        forceMinecraftCommand = getBoolean("settings.misc.force-minecraft-command", forceMinecraftCommand,
            "Whether to force the use of vanilla commands over plugin commands.");
        disableLeafDecay = getBoolean("settings.misc.disable-leaf-decay", disableLeafDecay,
            "Disables leaf block decay.");
        commandBlockParseResultsCaching = getBoolean("settings.misc.command-block-parse-results-caching", commandBlockParseResultsCaching,
            "Caches the parse results of command blocks, can significantly reduce performance impact.");
    }

    public static boolean disableDisconnectSpam = false;
    public static boolean connectionFlushQueueRewrite = false;
    public static boolean gracefulTeleportHandling  = false;
    public static boolean dontRespondPingBeforeStart = true;
    private static void networkSettings() {
        disableDisconnectSpam = getBoolean("settings.network.disable-disconnect-spam", disableDisconnectSpam,
            "Prevents players being disconnected by 'disconnect.spam' when sending too many chat packets");
        connectionFlushQueueRewrite = getBoolean("settings.network.connection-flush-queue-rewrite", connectionFlushQueueRewrite,
            "Replaces ConcurrentLinkedQueue with ArrayDeque in Connection for better performance",
            "and also uses the Netty event loop to ensure thread safety.",
            "",
            "Note: May increase the Netty thread usage");
        gracefulTeleportHandling  = getBoolean("settings.network.graceful-teleport-handling", gracefulTeleportHandling ,
            "Disables being disconnected from 'multiplayer.disconnect.invalid_player_movement' (also declines the packet handling).");
        dontRespondPingBeforeStart = getBoolean("settings.network.dont-respond-ping-before-start", dontRespondPingBeforeStart,
            "Prevents the server from responding to pings before the server is fully booted.");
    }

    public static boolean enableFasterTntOptimization = true;
    public static boolean explosionNoBlockDamage = false;
    public static double tntRandomRange = -1;
    private static void tntOptimization() {
        enableFasterTntOptimization = getBoolean("settings.tnt-optimization.enable-faster-tnt-optimization", enableFasterTntOptimization);
        explosionNoBlockDamage = getBoolean("settings.tnt-optimization.explosion-no-block-damage", explosionNoBlockDamage);
        tntRandomRange = getDouble("settings.tnt-optimization.tnt-random-range", tntRandomRange);
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
        lagCompensationEnabled = getBoolean("settings.lag-compensation.enabled", lagCompensationEnabled, "Improves the player experience when TPS is low");
        blockEntityAcceleration = getBoolean("settings.lag-compensation.block-entity-acceleration", blockEntityAcceleration);
        blockBreakingAcceleration = getBoolean("settings.lag-compensation.block-breaking-acceleration", blockBreakingAcceleration);
        eatingAcceleration = getBoolean("settings.lag-compensation.eating-acceleration", eatingAcceleration);
        potionEffectAcceleration = getBoolean("settings.lag-compensation.potion-effect-acceleration", potionEffectAcceleration);
        fluidAcceleration = getBoolean("settings.lag-compensation.fluid-acceleration", fluidAcceleration);
        pickupAcceleration = getBoolean("settings.lag-compensation.pickup-acceleration", pickupAcceleration);
        portalAcceleration = getBoolean("settings.lag-compensation.portal-acceleration", portalAcceleration);
        timeAcceleration = getBoolean("settings.lag-compensation.time-acceleration", timeAcceleration);
        randomTickSpeedAcceleration = getBoolean("settings.lag-compensation.random-tick-speed-acceleration", randomTickSpeedAcceleration);
    }

    public static boolean noChatReportsEnabled = false;
    public static boolean noChatReportsAddQueryData = true;
    public static boolean noChatReportsConvertToGameMessage = true;
    public static boolean noChatReportsDebugLog = false;
    public static boolean noChatReportsDemandOnClient = false;
    public static String noChatReportsDisconnectDemandOnClientMessage = "You do not have No Chat Reports, and this server is configured to require it on client!";
    private static void noChatReports() {
        noChatReportsEnabled = getBoolean("settings.no-chat-reports.enabled", noChatReportsEnabled,
            "Enables or disables the No Chat Reports feature");
        noChatReportsAddQueryData = getBoolean("settings.no-chat-reports.add-query-data", noChatReportsAddQueryData,
            "Should server include extra query data to help clients know that your server is secure");
        noChatReportsConvertToGameMessage = getBoolean("settings.no-chat-reports.convert-to-game-message", noChatReportsConvertToGameMessage,
            "Should the server convert all player messages to system messages");
        noChatReportsDebugLog = getBoolean("settings.no-chat-reports.debug-log", noChatReportsDebugLog);
        noChatReportsDemandOnClient = getBoolean("settings.no-chat-reports.demand-on-client", noChatReportsDemandOnClient,
            "Should the server require No Chat Reports on the client side");
        noChatReportsDisconnectDemandOnClientMessage = getString("settings.no-chat-reports.disconnect-demand-on-client-message", noChatReportsDisconnectDemandOnClientMessage,
            "Message to send to the client when they are disconnected for not having No Chat Reports");
    }

    public static boolean virtualThreadsEnabled = false;
    public static boolean virtualBukkitScheduler = false;
    public static boolean virtualChatScheduler = false;
    public static boolean virtualAuthenticatorScheduler = false;
    public static boolean virtualTabCompleteScheduler = false;
    public static boolean virtualAsyncExecutor = false;
    public static boolean virtualCommandBuilderScheduler = false;
    public static boolean virtualProfileLookupPool = false;
    public static boolean virtualServerTextFilterPool = false;
    private static void virtualThreads() {
        virtualThreadsEnabled = getBoolean("settings.virtual-threads.enabled", virtualThreadsEnabled,
            "Enables use of virtual threads that was added in Java 21");

        virtualBukkitScheduler = getBoolean("settings.virtual-threads.bukkit-scheduler", virtualBukkitScheduler,
            "Uses virtual threads for the Bukkit scheduler.");
        virtualChatScheduler = getBoolean("settings.virtual-threads.chat-scheduler", virtualChatScheduler,
            "Uses virtual threads for the Chat scheduler.");
        virtualAuthenticatorScheduler = getBoolean("settings.virtual-threads.authenticator-scheduler", virtualAuthenticatorScheduler,
            "Uses virtual threads for the Authenticator scheduler.");
        virtualTabCompleteScheduler = getBoolean("settings.virtual-threads.tab-complete-scheduler", virtualTabCompleteScheduler,
            "Uses virtual threads for the Tab Complete scheduler.");
        virtualAsyncExecutor = getBoolean("settings.virtual-threads.async-executor", virtualAsyncExecutor,
            "Uses virtual threads for the MCUtil async executor.");
        virtualCommandBuilderScheduler = getBoolean("settings.virtual-threads.command-builder-scheduler", virtualCommandBuilderScheduler,
            "Uses virtual threads for the Async Command Builder Thread Pool.");
        virtualProfileLookupPool = getBoolean("settings.virtual-threads.profile-lookup-pool", virtualProfileLookupPool,
            "Uses virtual threads for the Profile Lookup Pool, that is used for fetching player profiles.");
        virtualServerTextFilterPool = getBoolean("settings.virtual-threads.server-text-filter-pool", virtualServerTextFilterPool,
            "Uses virtual threads for the server text filter pool.");
    }

    public static boolean asyncPathfinding = true;
    public static int asyncPathfindingMaxThreads = 2;
    public static int asyncPathfindingKeepalive = 60;
    public static int asyncPathfindingQueueSize = 0;
    public static PathfindTaskRejectPolicy asyncPathfindingRejectPolicy = PathfindTaskRejectPolicy.FLUSH_ALL;
    private static void asyncPathfinding() {
        asyncPathfinding = getBoolean("settings.async-pathfinding.enable", asyncPathfinding);
        asyncPathfindingMaxThreads = getInt("settings.async-pathfinding.max-threads", asyncPathfindingMaxThreads);
        asyncPathfindingKeepalive = getInt("settings.async-pathfinding.keepalive", asyncPathfindingKeepalive);
        asyncPathfindingQueueSize = getInt("settings.async-pathfinding.queue-size", asyncPathfindingQueueSize);

        final int maxThreads = Runtime.getRuntime().availableProcessors();
        if (asyncPathfindingMaxThreads < 0) {
            asyncPathfindingMaxThreads = Math.max(maxThreads + asyncPathfindingMaxThreads, 1);
        } else if (asyncPathfindingMaxThreads == 0) {
            asyncPathfindingMaxThreads = Math.max(maxThreads / 4, 1);
        }

        if (!asyncPathfinding) {
            asyncPathfindingMaxThreads = 0;
        } else {
            LOGGER.info("Using {} threads for Async Pathfinding", asyncPathfindingMaxThreads);
        }

        if (asyncPathfindingQueueSize <= 0) asyncPathfindingQueueSize = asyncPathfindingMaxThreads * 256;

        asyncPathfindingRejectPolicy = PathfindTaskRejectPolicy.fromString(getString("settings.async-pathfinding.reject-policy", maxThreads >= 12 && asyncPathfindingQueueSize < 512 ? PathfindTaskRejectPolicy.FLUSH_ALL.toString() : PathfindTaskRejectPolicy.CALLER_RUNS.toString(),
            "The policy to use when the queue is full and a new task is submitted.",
            "FLUSH_ALL: All pending tasks will be run on server thread.",
            "CALLER_RUNS: Newly submitted task will be run on server thread."));
    }

    public static boolean multithreadedEnabled = true;
    public static boolean multithreadedCompatModeEnabled = false;
    public static int asyncEntityTrackerMaxThreads = 1;
    public static int asyncEntityTrackerKeepalive = 60;
    public static int asyncEntityTrackerQueueSize = 0;
    private static void multithreadedTracker() {
        multithreadedEnabled = getBoolean("settings.multithreaded-tracker.enable", multithreadedEnabled,
            "Make entity tracking saving asynchronously, can improve performance significantly,",
            "especially in some massive entities in small area situations.");
        multithreadedCompatModeEnabled = getBoolean("settings.multithreaded-tracker.compat-mode", multithreadedCompatModeEnabled,
            "Enable compat mode ONLY if Citizens or NPC plugins using real entity has installed.",
            "Compat mode fixes visible issues with player type NPCs of Citizens.",
            "But we recommend to use packet based / virtual entity NPC plugin, e.g. ZNPC Plus, Adyeshach, Fancy NPC and etc.");

        asyncEntityTrackerMaxThreads = getInt("settings.multithreaded-tracker.max-threads", asyncEntityTrackerMaxThreads);
        asyncEntityTrackerKeepalive = getInt("settings.multithreaded-tracker.keepalive", asyncEntityTrackerKeepalive);
        asyncEntityTrackerQueueSize = getInt("settings.multithreaded-tracker.queue-size", asyncEntityTrackerQueueSize);

        if (asyncEntityTrackerMaxThreads < 0) {
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() + asyncEntityTrackerMaxThreads, 1);
        } else if (asyncEntityTrackerMaxThreads == 0) {
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
        }

        if (!multithreadedEnabled) {
            asyncEntityTrackerMaxThreads = 0;
        } else {
            LOGGER.info("Using {} threads for Async Entity Tracker", asyncEntityTrackerMaxThreads);
        }

        if (asyncEntityTrackerQueueSize <= 0) asyncEntityTrackerQueueSize = asyncEntityTrackerMaxThreads * 384;
    }
}
