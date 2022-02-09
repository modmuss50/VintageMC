package vintagemc.loom.mcp;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.LoomGradlePlugin;
import net.fabricmc.loom.api.mappings.intermediate.IntermediateMappingsProvider;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.util.FileSystemUtil;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.SrgReader;
import net.fabricmc.mappingio.tree.ClassAnalysisDescCompleter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class MCPSrgProvider extends IntermediateMappingsProvider {
    @Inject
    public abstract Project getProject();

    @Override
    public void provide(Path tinyMappings) throws IOException {
        if (Files.exists(tinyMappings) && !LoomGradlePlugin.refreshDeps) {
            return;
        }

        final Path mcpZip = McpUtils.getMcpZipPath(getProject());
        final MemoryMappingTree mappingTree = new MemoryMappingTree();

        try (FileSystemUtil.Delegate fs = FileSystemUtil.getJarFileSystem(mcpZip, true)) {
            for (String path : getMappingPaths()) {
                try (Reader reader = Files.newBufferedReader(fs.get().getPath(path))) {
                    SrgReader.read(reader, MappingsNamespace.OFFICIAL.toString(), MappingsNamespace.INTERMEDIARY.toString(), mappingTree);
                }
            }
        }

        for (Path jar : LoomGradleExtension.get(getProject()).getMinecraftJars(MappingsNamespace.OFFICIAL)) {
            ClassAnalysisDescCompleter.process(jar, MappingsNamespace.OFFICIAL.toString(), mappingTree);
        }

        try (MappingWriter writer = MappingWriter.create(tinyMappings, MappingFormat.TINY_2)) {
            mappingTree.accept(writer);
        }
    }

    private List<String> getMappingPaths() {
        final LoomGradleExtension extension = LoomGradleExtension.get(getProject());

        return switch (extension.getMinecraftJarConfiguration().get()) {
            case MERGED -> List.of("conf/client.srg", "conf/server.srg");
            case SERVER_ONLY -> List.of("conf/server.srg");
            case CLIENT_ONLY -> List.of("conf/client.srg");
            case SPLIT -> throw new UnsupportedOperationException("Split configuration is unsupported");
        };
    }

    @Override
    public String getName() {
        return "mcp-srg";
    }
}
