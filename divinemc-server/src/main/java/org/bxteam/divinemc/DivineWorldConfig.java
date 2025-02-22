package org.bxteam.divinemc;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;

import static org.bxteam.divinemc.DivineConfig.log;

@SuppressWarnings("unused")
@NullMarked
public final class DivineWorldConfig {
    private final YamlConfiguration config;
    private final String worldName;
    private final World.Environment environment;

    public DivineWorldConfig(String worldName, World.Environment environment) {
        this.config = DivineConfig.config;
        this.worldName = worldName;
        this.environment = environment;
        init();
    }

    public void init() {
        log("-------- World Settings For [" + worldName + "] --------");
        DivineConfig.readConfig(DivineWorldConfig.class, this);
    }

    private void set(String path, Object val) {
        this.config.addDefault("world-settings.default." + path, val);
        this.config.set("world-settings.default." + path, val);
        if (this.config.get("world-settings." + worldName + "." + path) != null) {
            this.config.addDefault("world-settings." + worldName + "." + path, val);
            this.config.set("world-settings." + worldName + "." + path, val);
        }
    }

    private ConfigurationSection getConfigurationSection(String path) {
        ConfigurationSection section = this.config.getConfigurationSection("world-settings." + worldName + "." + path);
        return section != null ? section : this.config.getConfigurationSection("world-settings.default." + path);
    }

    private String getString(String path, String def) {
        this.config.addDefault("world-settings.default." + path, def);
        return this.config.getString("world-settings." + worldName + "." + path, this.config.getString("world-settings.default." + path));
    }

    private boolean getBoolean(String path, boolean def) {
        this.config.addDefault("world-settings.default." + path, def);
        return this.config.getBoolean("world-settings." + worldName + "." + path, this.config.getBoolean("world-settings.default." + path));
    }

    private double getDouble(String path, double def) {
        this.config.addDefault("world-settings.default." + path, def);
        return this.config.getDouble("world-settings." + worldName + "." + path, this.config.getDouble("world-settings.default." + path));
    }

    private int getInt(String path, int def) {
        this.config.addDefault("world-settings.default." + path, def);
        return this.config.getInt("world-settings." + worldName + "." + path, this.config.getInt("world-settings.default." + path));
    }

    private <T> List<?> getList(String path, T def) {
        this.config.addDefault("world-settings.default." + path, def);
        return this.config.getList("world-settings." + worldName + "." + path, this.config.getList("world-settings.default." + path));
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
