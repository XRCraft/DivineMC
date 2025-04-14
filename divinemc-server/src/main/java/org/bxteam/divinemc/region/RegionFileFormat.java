package org.bxteam.divinemc.region;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import java.util.Locale;

public enum RegionFileFormat {
    LINEAR(".linear"),
    ANVIL(".mca"),
    UNKNOWN(null);

    private final String extension;

    RegionFileFormat(String extension) {
        this.extension = extension;
    }

    public String getExtensionName() {
        return this.extension;
    }

    @Contract(pure = true)
    public static RegionFileFormat fromName(@NotNull String name) {
        switch (name.toUpperCase(Locale.ROOT)) {
            case "MCA", "ANVIL" -> {
                return ANVIL;
            }

            case "LINEAR" -> {
                return LINEAR;
            }

            default -> {
                throw new IllegalArgumentException("Unknown region file format: " + name);
            }
        }
    }

    @Contract(pure = true)
    public static RegionFileFormat fromExtension(@NotNull String name) {
        switch (name.toLowerCase()) {
            case "mca", "anvil" -> {
                return ANVIL;
            }

            case "linear" -> {
                return LINEAR;
            }

            default -> {
                return UNKNOWN;
            }
        }
    }
}
