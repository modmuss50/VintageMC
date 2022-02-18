package vintagemc.fml.compat.coremod;

import cpw.mods.fml.relauncher.IClassTransformer;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreModLocator {
    public static final List<IFMLLoadingPlugin> LOADING_PLUGINS = new ArrayList<>();
    public static final List<IClassTransformer> CLASS_TRANSFORMERS = new ArrayList<>();
    public static final List<String> CLASS_TRANSFORMER_NAMES = new ArrayList<>();

    // Taken from RelaunchLibraryManager
    private static final String[] ROOT_PLUGINS = new String[]{"cpw.mods.fml.relauncher.FMLCorePlugin", "net.minecraftforge.classloading.FMLForgePlugin"};

    public static void setup() {
        try {
            for (String rootPlugin : ROOT_PLUGINS) {
                IFMLLoadingPlugin plugin = (IFMLLoadingPlugin) Class.forName(rootPlugin, true, CoreModLocator.class.getClassLoader()).getConstructor().newInstance();
                LOADING_PLUGINS.add(plugin);
            }

            // TODO check: fml.coreMods.load
            // TODO obey: IFMLLoadingPlugin.TransformerExclusions
            // TODO use: RelaunchLibraryManager.discoverCoreMods
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find coremod: ", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to construct coremod: ", e);
        }

        setupClassTransformers();
    }

    private static void setupClassTransformers() {
        List<String> asmTransformerClasses = LOADING_PLUGINS.stream().map(IFMLLoadingPlugin::getASMTransformerClass).flatMap(Arrays::stream).toList();

        for (String asmTransformerClass : asmTransformerClasses) {
            try {
                IClassTransformer classTransformer = (IClassTransformer) Class.forName(asmTransformerClass, true, CoreModLocator.class.getClassLoader()).getConstructor().newInstance();
                CLASS_TRANSFORMERS.add(classTransformer);
                CLASS_TRANSFORMER_NAMES.add(asmTransformerClass);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException("Failed to construct class transformer: " + asmTransformerClass, e);
            }
        }
    }
}
