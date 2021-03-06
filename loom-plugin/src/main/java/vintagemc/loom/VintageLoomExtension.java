package vintagemc.loom;

import net.fabricmc.loom.LoomGradleExtension;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import vintagemc.loom.forge.ForgeJarProcessor;
import vintagemc.loom.mcp.MCPMappingsSpec;
import vintagemc.loom.mcp.MCPSrgProvider;

import javax.inject.Inject;

public abstract class VintageLoomExtension {
    @Inject
    public abstract Project getProject();

    public abstract RegularFileProperty getMcpZip();

    public MCPMappingsSpec mcp() {
        return new MCPMappingsSpec(1);
    }

    public void withForge(String mavenNotation) {
        ForgeJarProcessor.setup(getProject(), mavenNotation);
    }

    public void withSrg() {
        final LoomGradleExtension loomExtension = LoomGradleExtension.get(getProject());
        loomExtension.setIntermediateMappingsProvider(MCPSrgProvider.class, provider -> {});
    }

    public static VintageLoomExtension get(Project project) {
        return project.getExtensions().getByType(VintageLoomExtension.class);
    }
}
