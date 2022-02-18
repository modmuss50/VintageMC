package vintagemc.loom.forge;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;
import net.fabricmc.loom.configuration.processors.JarProcessor;
import net.fabricmc.loom.util.FileSystemUtil;
import net.fabricmc.loom.util.TinyRemapperHelper;
import net.fabricmc.loom.util.ZipUtils;
import net.fabricmc.lorenztiny.TinyMappingsReader;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.cadixdev.at.AccessTransformSet;
import org.cadixdev.lorenz.MappingSet;
import org.gradle.api.Project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class ForgeJarProcessor implements JarProcessor {
    private static final Map<String, String> FML_SIDE_TO_LOADER = new ImmutableMap.Builder<String, String>()
            .put("cpw/mods/fml/relauncher/SideOnly", "net/fabricmc/api/Environment")
            .put("cpw/mods/fml/relauncher/Side", "net/fabricmc/api/EnvType")
            .build();



    public static void setup(Project project, String mavenNotation) {
        File forgeJar = project.getConfigurations().detachedConfiguration(project.getDependencies().create(mavenNotation)).getSingleFile();

        LoomGradleExtension loomGradleExtension = LoomGradleExtension.get(project);
        loomGradleExtension.getGameJarProcessors().add(new ForgeJarProcessor(project, forgeJar.toPath(), mavenNotation));
    }

    private final Project project;
    private final Path forgeZip;
    private final String mavenNotation;

    public ForgeJarProcessor(Project project, Path forgeZip, String mavenNotation) {
        this.project = project;
        this.forgeZip = forgeZip;
        this.mavenNotation = mavenNotation;
    }

    @Override
    public String getId() {
        return "Forge:" + mavenNotation;
    }

    @Override
    public void process(File targetFile) {
        try {
            final Path remappedForgeJar = getRemappedForgeJar();

            try (FileSystemUtil.Delegate outputFs = FileSystemUtil.getJarFileSystem(targetFile, true);
                 FileSystemUtil.Delegate inputFs = FileSystemUtil.getJarFileSystem(remappedForgeJar, true);
                 Stream<Path> walk = Files.walk(inputFs.get().getPath("/"))) {

                Iterator<Path> iterator = walk.iterator();

                // Copy all of the inputs to the output zip, replacing any existing.
                while (iterator.hasNext()) {
                    Path fsPath = iterator.next();

                    if (!Files.isRegularFile(fsPath)) continue;
                    Path dstPath = outputFs.get().getPath("/").resolve(inputFs.get().getPath("/").relativize(fsPath).toString());
                    Path dstPathParent = dstPath.getParent();
                    if (dstPathParent != null) Files.createDirectories(dstPathParent);
                    Files.copy(fsPath, dstPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }

            try (FileSystemUtil.Delegate outputFs = FileSystemUtil.getJarFileSystem(targetFile, true);
                 Stream<Path> walk = Files.walk(outputFs.get().getPath("/"))) {
                Iterator<Path> iterator = walk.iterator();
                while (iterator.hasNext()) {
                    Path fsPath = iterator.next();

                    if (!Files.isRegularFile(fsPath)) continue;

                    if (fsPath.startsWith("/argo")) {
                        // Unwanted files in the original jar.
                        Files.delete(fsPath);
                    }
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to process forge jar", e);
        }
    }

    private Path getRemappedForgeJar() throws IOException {
        final Path output = Files.createTempFile("forge", ".jar");
        Files.delete(output);

        final TinyRemapper tinyRemapper = TinyRemapperHelper.getTinyRemapper(project, MappingsNamespace.OFFICIAL.toString(), MappingsNamespace.NAMED.toString(), false,
                builder -> builder.withMappings(out -> FML_SIDE_TO_LOADER.forEach(out::acceptClass))
        );
        tinyRemapper.readClassPath(TinyRemapperHelper.getMinecraftDependencies(project));

        for (Path minecraftJar : LoomGradleExtension.get(project).getMinecraftJars(MappingsNamespace.OFFICIAL)) {
            tinyRemapper.readClassPath(minecraftJar);
        }

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).build()) {
            outputConsumer.addNonClassFiles(forgeZip, NonClassCopyMode.FIX_META_INF, tinyRemapper);
            tinyRemapper.readInputs(forgeZip);
            tinyRemapper.apply(outputConsumer);
        } finally {
            tinyRemapper.finish();
        }

        final LoomGradleExtension extension = LoomGradleExtension.get(project);
        final MappingSet mappingSet;

        try (TinyMappingsReader reader = new TinyMappingsReader(extension.getMappingsProvider().getMappings(), MappingsNamespace.OFFICIAL.toString(), MappingsNamespace.NAMED.toString())) {
            mappingSet = reader.read();
        }

        ZipUtils.transformString(output, Map.of(
            "fml_at.cfg", remapAT(mappingSet),
            "forge_at.cfg", remapAT(mappingSet)
        ));

        return output;
    }

    private ZipUtils.UnsafeUnaryOperator<String> remapAT(MappingSet mappingSet) {
        return arg -> {
            AccessTransformSet accessTransformer;

            try (BufferedReader reader = new BufferedReader(new StringReader(arg))) {
                accessTransformer = FmlLegacyATReader.read(reader);
            }

            accessTransformer = accessTransformer.remap(mappingSet);

            final Writer writer = new StringWriter();

            try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                new FmlLegacyATWriter(bufferedWriter).write(accessTransformer);
            }

            return writer.toString();
        };
    }

    @Override
    public void setup() {

    }
}
