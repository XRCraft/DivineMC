package org.bxteam.divinemc;

import io.papermc.paper.ServerBuildInfo;
import joptsimple.OptionSet;
import net.minecraft.SharedConstants;
import net.minecraft.server.Eula;
import net.minecraft.server.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DivineBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger("DivineBootstrap");

    public static void boot(final OptionSet options) {
        SharedConstants.tryDetectVersion();
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
        getStartupVersionMessages().forEach(LOGGER::info);

        Main.main(options);
    }

    private static List<String> getStartupVersionMessages() {
        final String javaSpecVersion = System.getProperty("java.specification.version");
        final String javaVmName = System.getProperty("java.vm.name");
        final String javaVmVersion = System.getProperty("java.vm.version");
        final String javaVendor = System.getProperty("java.vendor");
        final String javaVendorVersion = System.getProperty("java.vendor.version");
        final String osName = System.getProperty("os.name");
        final String osVersion = System.getProperty("os.version");
        final String osArch = System.getProperty("os.arch");

        final ServerBuildInfo bi = ServerBuildInfo.buildInfo();
        return List.of(
            String.format(
                "Running Java %s (%s %s; %s %s) on %s %s (%s)",
                javaSpecVersion,
                javaVmName,
                javaVmVersion,
                javaVendor,
                javaVendorVersion,
                osName,
                osVersion,
                osArch
            ),
            String.format(
                "Loading %s %s for Minecraft %s",
                bi.brandName(),
                bi.asString(ServerBuildInfo.StringRepresentation.VERSION_FULL),
                bi.minecraftVersionId()
            ),
            String.format(
                "Running JVM args %s",
                ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
            )
        );
    }
}
