package vintagemc.loom.mcp;

import net.fabricmc.loom.api.mappings.layered.MappingContext;
import net.fabricmc.loom.api.mappings.layered.spec.MappingsSpec;
import net.fabricmc.loom.configuration.providers.mappings.GradleMappingContext;

import java.io.IOException;
import java.io.UncheckedIOException;

public record MCPMappingsSpec(int v) implements MappingsSpec<MCPMappingsLayer> {
    @Override
    public MCPMappingsLayer createLayer(MappingContext context) {
        // TODO fix this to support unit testing?
        final GradleMappingContext gradleContext = (GradleMappingContext) context;

        try {
            return new MCPMappingsLayer(McpUtils.getMcpZipPath(gradleContext.getProject()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
