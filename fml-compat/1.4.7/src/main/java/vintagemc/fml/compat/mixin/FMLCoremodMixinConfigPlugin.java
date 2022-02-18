package vintagemc.fml.compat.mixin;

import cpw.mods.fml.relauncher.IClassTransformer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.knot.MixinServiceKnot;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.ReEntranceLock;
import vintagemc.fml.compat.coremod.CoreModLocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class FMLCoremodMixinConfigPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_NAME = "vintagemc/fml/compat/mixin/FmlCoreModMixin";
    private static final String ENVIRONMENT_DESCRIPTOR = "Lnet/fabricmc/api/Environment;";
    private static final List<String> EXCLUSIONS = List.of("cpw.mods.fml.relauncher.", "net.fabricmc.api.", "vintagemc.");
    private static ReEntranceLock reEntranceLock;

    @Override
    public void onLoad(String mixinPackage) {
        List<String> classes = getAllClassFiles();

        classes.removeIf(s -> {
            String className = s.replace("/", ".");
            return EXCLUSIONS.stream().anyMatch(className::startsWith);
        });

        byte[] mixinBlob = makeMixinBlob(MIXIN_NAME, classes);

        try {
            // This could be the worse code you read today.
            final Path tempJar = Files.createTempFile("mixin", ".jar");
            Files.delete(tempJar);

            try (FileSystem fs = getJarFileSystem(tempJar.toUri(), true)) {
                Path target = fs.getPath(MIXIN_NAME + ".class");
                Files.createDirectories(target.getParent());
                Files.write(target, mixinBlob);
            }

            Method defineClass = getClass().getClassLoader().getParent().getClass().getDeclaredMethod("addURL", URL.class);
            defineClass.setAccessible(true);
            defineClass.invoke(getClass().getClassLoader().getParent(), tempJar.toUri().toURL());
        } catch (Exception e) {
           throw new RuntimeException(e);
        }

        try {
            reEntranceLock = getLockObject();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getAllClassFiles() {
        List<String> classes = new ArrayList<>();
        Path minecraftJar = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow().getOrigin().getPaths().get(0);

        try (FileSystem fs = getJarFileSystem(minecraftJar.toUri(), false);
             Stream<Path> walk = Files.walk(fs.getPath("/"))) {
            Iterator<Path> iterator = walk.iterator();

            while (iterator.hasNext()) {
                Path fsPath = iterator.next();

                if (fsPath.toString().endsWith(".class")) {
                    if (!validForCurrentSide(fsPath)) continue;

                    classes.add(fsPath.toString().substring(1).replace(".class", ""));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return classes;
    }

    private boolean validForCurrentSide(Path classfile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(classfile)) {
            ClassReader reader = new ClassReader(inputStream);

            final AtomicBoolean valid = new AtomicBoolean(true);
            // Get this from mixin to prevent class loading issues.
            final String currentSide = MixinEnvironment.getCurrentEnvironment().getSide().toString();

            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (ENVIRONMENT_DESCRIPTOR.equals(descriptor)) {
                        return new AnnotationVisitor(api) {
                            @Override
                            public void visitEnum(String name, String descriptor, String value) {


                                if ("value".equals(name) && !currentSide.equals(value)) {
                                    valid.set(false);
                                }
                            }
                        };
                    }

                    return super.visitAnnotation(descriptor, visible);
                }
            };

            reader.accept(classVisitor, 0);

            return valid.get();
        }
    }

    private ReEntranceLock getLockObject() throws NoSuchFieldException, IllegalAccessException {
        Field transformerField = MixinServiceKnot.class.getDeclaredField("transformer");
        transformerField.setAccessible(true);
        Object /* MixinTransformer */ transformer = transformerField.get(null);

        Field processorField = transformer.getClass().getDeclaredField("processor");
        processorField.setAccessible(true);
        Object /* MixinProcessor */ processor = processorField.get(transformer);

        Field lockField = processor.getClass().getDeclaredField("lock");
        lockField.setAccessible(true);

        return (ReEntranceLock) lockField.get(processor);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of("FmlCoreModMixin");
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (CoreModLocator.CLASS_TRANSFORMER_NAMES.contains(targetClassName) || EXCLUSIONS.stream().anyMatch(targetClassName::startsWith)) {
            return;
        }

        targetClass.interfaces.remove("vintagemc/fml/compat/mixin/FmlCoreModMixin");

        byte[] bytes = writeClassToBytes(targetClass);

        // I'm sure this is fine :)
        reEntranceLock.pop();

        for (IClassTransformer classTransformer : CoreModLocator.CLASS_TRANSFORMERS) {
            bytes = classTransformer.transform(targetClassName, bytes);
        }

        reEntranceLock.push();

        ClassNode transformedClass = readClassFromBytes(bytes);

        targetClass.access = transformedClass.access;
        targetClass.methods = transformedClass.methods;
        targetClass.fields = transformedClass.fields;
        targetClass.interfaces = transformedClass.interfaces;
        targetClass.superName = transformedClass.superName;
        targetClass.innerClasses = transformedClass.innerClasses;
        targetClass.visibleAnnotations = transformedClass.visibleAnnotations;
        targetClass.invisibleAnnotations = transformedClass.invisibleAnnotations;
        targetClass.visibleTypeAnnotations = transformedClass.visibleTypeAnnotations;
        targetClass.invisibleTypeAnnotations = transformedClass.invisibleTypeAnnotations;
        targetClass.attrs = transformedClass.attrs;
    }

    private static byte[] writeClassToBytes(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static ClassNode readClassFromBytes(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, ClassReader.SKIP_FRAMES);
        return classNode;
    }

    /**
     * Taken from https://github.com/Chocohead/Fabric-ASM. Thanks :)
     */
    private static byte[] makeMixinBlob(String name, Collection<? extends String> targets) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(52, Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE, name, null, "java/lang/Object", null);

        AnnotationVisitor mixinAnnotation = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        AnnotationVisitor targetAnnotation = mixinAnnotation.visitArray("value");
        for (String target : targets) targetAnnotation.visit(null, Type.getType('L' + target + ';'));
        targetAnnotation.visitEnd();
        mixinAnnotation.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static FileSystem getJarFileSystem(URI uri, boolean create) throws IOException {
        URI jarUri;

        try {
            jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        return FileSystems.newFileSystem(jarUri, create ? Map.of("create", "true") : Collections.emptyMap());
    }
}
