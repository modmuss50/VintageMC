package vintagemc.loom;

import net.fabricmc.loom.LoomGradleExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import vintagemc.loom.mcp.MCPSrgProvider;
import vintagemc.loom.mcp.McpUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

@SuppressWarnings("unused")
public class VintageLoomPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getLogger().lifecycle("Vintage loom");

        project.getExtensions().create("vintageLoom", VintageLoomExtension.class);

        final LoomGradleExtension loomExtension = LoomGradleExtension.get(project);
        loomExtension.setIntermediateMappingsProvider(MCPSrgProvider.class, provider -> {});

        project.afterEvaluate(p -> {
            // Just ensure the mcp zip is present
            try {
                McpUtils.getMcpZipPath(p);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
