package vintagemc.loom.mcp;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.configuration.providers.minecraft.MinecraftJarConfiguration;
import net.fabricmc.loom.util.ZipUtils;
import org.gradle.api.Project;
import vintagemc.loom.VintageLoomExtension;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class McpUtils {
    public static Path getMcpZipPath(Project project) throws IOException {
        VintageLoomExtension extension = VintageLoomExtension.get(project);
        Path path = extension.getMcpZip().get().getAsFile().toPath();

        if (Files.notExists(path)) {
            throw new FileNotFoundException(path.toString());
        }

        return path;
    }

    public static void validateVersion(Path zip, Project project) throws IOException {
        final MinecraftJarConfiguration jarConfig = LoomGradleExtension.get(project).getMinecraftJarConfiguration().get();

        final String clientVersion = getConfigValue(zip, "ClientVersion");
        final String serverVersion = getConfigValue(zip, "ServerVersion");
    }

    public static String getConfigValue(Path zip, String key) throws IOException {
        byte[] versionCfgBytes = ZipUtils.unpack(zip, "conf/version.cfg");
        Pattern pattern = Pattern.compile("^%s = (?<version>.*)$".formatted(key));

        return "";
    }

}
