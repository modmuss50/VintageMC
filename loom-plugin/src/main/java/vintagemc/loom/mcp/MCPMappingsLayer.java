package vintagemc.loom.mcp;

import net.fabricmc.loom.api.mappings.layered.MappingLayer;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.util.FileSystemUtil;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record MCPMappingsLayer(Path mcpZip) implements MappingLayer {
    @Override
    public void visit(MappingVisitor mappingVisitor) throws IOException {
        // TODO massive hack!! this will explode in my face at somepoint
        MemoryMappingTree memoryMappingTree = (MemoryMappingTree) mappingVisitor;

        try (FileSystemUtil.Delegate fs = FileSystemUtil.getJarFileSystem(mcpZip, true)) {
            try (BufferedReader reader = Files.newBufferedReader(fs.get().getPath("conf/methods.csv"))) {
                readMethods(reader, memoryMappingTree);
            }

            try (BufferedReader reader = Files.newBufferedReader(fs.get().getPath("conf/fields.csv"))) {
                readFields(reader, memoryMappingTree);
            }
        }
    }

    private void readMethods(BufferedReader reader, MemoryMappingTree tree) throws IOException {
        String header = reader.readLine();

        if (!"searge,name,side,desc".equals(header)) {
            throw new IllegalStateException("Unexpected methods.csv header");
        }

        final int srgNs = tree.getNamespaceId(MappingsNamespace.INTERMEDIARY.toString());
        final int dstNs = tree.getNamespaceId(MappingsNamespace.NAMED.toString());

        String line;
        while ((line = reader.readLine()) != null) {
            final String[] split = line.split(",");

            final String srg = split[0];
            final String name = split[1];
            final String desc = split.length == 4 ? split[3] : "";

            addMethodMapping(tree, srg, name, desc, srgNs, dstNs);
        }
    }

    private void addMethodMapping(MemoryMappingTree tree, String srg, String name, String desc, int srgNs, int dstNs) {
        for (MemoryMappingTree.ClassMapping classMapping : tree.getClasses()){
            for (MappingTree.MethodMapping method : classMapping.getMethods()) {
                if (method.getName(srgNs).equals(srg)) {
                    method.setDstName(name, dstNs);

                    if (!desc.isEmpty()) {
                        method.setComment(desc);
                    }
                }
            }
        }
    }

    private void readFields(BufferedReader reader, MemoryMappingTree tree) throws IOException {
        final String header = reader.readLine();

        if (!"searge,name,side,desc".equals(header)) {
            throw new IllegalStateException("Unexpected methods.csv header");
        }

        final int srgNs = tree.getNamespaceId(MappingsNamespace.INTERMEDIARY.toString());
        final int dstNs = tree.getNamespaceId(MappingsNamespace.NAMED.toString());

        String line;
        while ((line = reader.readLine()) != null) {
            final String[] split = line.split(",");

            final String srg = split[0];
            final String name = split[1];
            final String desc = split.length == 4 ? split[3] : "";

            addFieldMapping(tree, srg, name, desc, srgNs, dstNs);
        }
    }

    private void addFieldMapping(MemoryMappingTree tree, String srg, String name, String desc, int srgNs, int dstNs) {
        for (MappingTree.ClassMapping classMapping : tree.getClasses()){
            for (MappingTree.FieldMapping field : classMapping.getFields()) {
                if (field.getName(srgNs).equals(srg)) {
                    field.setDstName(name, dstNs);

                    if (!desc.isEmpty()) {
                        field.setComment(desc);
                    }
                }
            }
        }
    }

    @Override
    public MappingsNamespace getSourceNamespace() {
        return MappingsNamespace.INTERMEDIARY;
    }
}
