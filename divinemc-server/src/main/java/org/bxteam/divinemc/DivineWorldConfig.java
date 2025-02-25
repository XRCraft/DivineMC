package org.bxteam.divinemc;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "DuplicatedCode"})
public class DivineWorldConfig {
    private static final String HEADER = """
        This is the world configuration file for DivineMC.
        Configuration options here apply to all worlds, unless you specify overrides inside
        the world-specific config file inside each world folder.
        
        If you need help with the configuration or have any questions related to DivineMC,
        join us in our Discord server.

        Discord: https://discord.gg/p7cxhw7E2M
        Docs: https://bxteam.org/docs/divinemc
        New builds: https://github.com/BX-Team/DivineMC/releases/latest""";

    public static final Logger LOGGER = LogManager.getLogger(DivineWorldConfig.class.getSimpleName());
    public static final int CONFIG_VERSION = 5;

    private static final YamlFile config = new YamlFile();
    private final String worldName;
    private final World.Environment environment;

    public DivineWorldConfig(String worldName, World.Environment environment) throws IOException {
        this.worldName = worldName;
        this.environment = environment;
        init();
    }

    public void init() throws IOException {
        File configFile = new File("config", "divinemc-world.yml");

		if (configFile.exists()) {
			try {
				config.load(configFile);
			} catch (InvalidConfigurationException e) {
				throw new IOException(e);
			}
		}

        config.getInt("config.version", CONFIG_VERSION);
        config.options().header(HEADER);

        for (Method method : DivineWorldConfig.class.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(this);
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                        LOGGER.error("Error invoking {}", method.getName(), ex);
                    }
                }
            }
        }

        try {
            config.save(configFile);
        } catch (IOException ex) {
            LOGGER.error("Could not save {}", configFile, ex);
        }
    }

    private void set(String path, Object val) {
        config.addDefault("world-settings.default." + path, val);
        config.set("world-settings.default." + path, val);
        if (config.get("world-settings." + worldName + "." + path) != null) {
            config.addDefault("world-settings." + worldName + "." + path, val);
            config.set("world-settings." + worldName + "." + path, val);
        }
    }

    private String getString(String path, String def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getString("world-settings." + worldName + "." + path, config.getString("world-settings.default." + path));
    }

    private boolean getBoolean(String path, boolean def) {
        config.addDefault("settings.world.default." + path, def);
        return config.getBoolean("world-settings." + worldName + "." + path, config.getBoolean("world-settings.default." + path));
    }

    private double getDouble(String path, double def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getDouble("world-settings." + worldName + "." + path, config.getDouble("world-settings.default." + path));
    }

    private int getInt(String path, int def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getInt("world-settings." + worldName + "." + path, config.getInt("world-settings.default." + path));
    }

    private <T> List<?> getList(String path, T def) {
        config.addDefault("world-settings.default." + path, def);
        return config.getList("world-settings." + worldName + "." + path, config.getList("world-settings.default." + path));
    }

    private Map<String, Object> getMap(String path, Map<String, Object> def) {
        final Map<String, Object> fallback = this.getMap("world-settings.default." + path, def);
        final Map<String, Object> value = this.getMap("world-settings." + worldName + "." + path, null);
        return value.isEmpty() ? fallback : value;
    }

    public boolean snowballCanKnockback = true;
    public boolean disableSnowballSaving = false;
    public boolean eggCanKnockback = true;
    public boolean disableFireworkSaving = false;
    private void projectilesSettings() {
        snowballCanKnockback = getBoolean("gameplay-mechanics.projectiles.snowball.knockback", snowballCanKnockback);
        disableSnowballSaving = getBoolean("gameplay-mechanics.projectiles.snowball.disable-saving", disableSnowballSaving);
        eggCanKnockback = getBoolean("gameplay-mechanics.projectiles.egg.knockback", eggCanKnockback);
        disableFireworkSaving = getBoolean("gameplay-mechanics.projectiles.firework.disable-saving", disableFireworkSaving);
    }

    public boolean allowEntityPortalWithPassenger = true;
    private void unsupportedFeatures() {
        allowEntityPortalWithPassenger = getBoolean("unsupported-features.allow-entity-portal-with-passenger", allowEntityPortalWithPassenger);
    }
}
