package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.modloader.BaseModProxy;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vintagemc.fml.compat.FMLPreLaunch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(ModClassLoader.class)
public class ModClassLoaderMixin extends URLClassLoader {
    @Unique
    private final ClassLoader target = FabricLauncherBase.getLauncher().getTargetClassLoader();
    private final List<URL> addedUrls = new ArrayList<>();

    public ModClassLoaderMixin(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Inject(method = "addFile", at = @At("HEAD"), remap = false)
    private void addFile(File modFile, CallbackInfo ci) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        URL url = modFile.toURI().toURL();
        // KnotClassLoader.DynamicURLClassLoader
        URLClassLoader classLoader = (URLClassLoader) FabricLauncherBase.getLauncher().getTargetClassLoader().getParent();
        Method addURLMethod = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
        addURLMethod.setAccessible(true);
        addURLMethod.invoke(classLoader, url);

        addedUrls.add(url);
    }

    @Overwrite(remap = false)
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return target.loadClass(name);
    }

    @Override
    public URL[] getURLs() {
        return addedUrls.toArray(URL[]::new);
    }

    @Override
    public URL findResource(final String name) {
        return target.getResource(name);
    }

    @Override
    public Enumeration<URL> findResources(final String name) throws IOException {
        return target.getResources(name);
    }

    @Inject(method = "loadBaseModClass", at = @At("HEAD"), remap = false)
    public void loadBaseModClass(String modClazzName, CallbackInfoReturnable<Class<? extends BaseModProxy>> cir) throws Exception {
        cir.setReturnValue((Class<? extends BaseModProxy>) Class.forName(modClazzName, true, FabricLauncherBase.getLauncher().getTargetClassLoader()));
    }
}
