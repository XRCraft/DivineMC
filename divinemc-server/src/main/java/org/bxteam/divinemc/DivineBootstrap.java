package org.bxteam.divinemc;

import io.papermc.paper.PaperBootstrap;
import joptsimple.OptionSet;
import net.minecraft.server.Eula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DivineBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

    public static void boot(final OptionSet options) {
        runPreBootTasks();

        // DivineMC start - Verify Minecraft EULA earlier
        Path path2 = Paths.get("eula.txt");
        Eula eula = new Eula(path2);
        boolean eulaAgreed = Boolean.getBoolean("com.mojang.eula.agree");
        if (eulaAgreed) {
            LOGGER.error("You have used the Spigot command line EULA agreement flag.");
            LOGGER.error("By using this setting you are indicating your agreement to Mojang's EULA (https://aka.ms/MinecraftEULA).");
            LOGGER.error("If you do not agree to the above EULA please stop your server and remove this flag immediately.");
        }
        if (!eula.hasAgreedToEULA() && !eulaAgreed) {
            LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
            return;
        }
        System.out.println("Loading libraries, please wait..."); // Restore CraftBukkit log
        // DivineMC end - Verify Minecraft EULA earlier

        PaperBootstrap.boot(options);
    }

    private static void runPreBootTasks() {
        if (getJavaVersion() > 21) {
            System.setProperty("jdk.console", "java.base");
        }
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        version = version.split("-")[0];
        return Integer.parseInt(version);
    }
}
