package vintagemc.fml.compat.mixin;

import cpw.mods.fml.relauncher.FMLRelauncher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@Mixin(FMLRelauncher.class)
public class FMLRelauncherMixin {
	@Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getClassLoader()Ljava/lang/ClassLoader;"))
	public ClassLoader redirectModClassLoader(Class instance) {
		ArrayList<URL> urls = new ArrayList<>();

		// Build up a classloader with the classpath on it, used to help find FML mods, not used for loading classes.
		for (String cpEntry : System.getProperty("java.class.path").split(File.pathSeparator)) {
			if (cpEntry.isEmpty() || cpEntry.endsWith("*")) continue;

			try {
				urls.add(Paths.get(cpEntry).toUri().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		// KnotClassLoader.DynamicURLClassLoader
		URLClassLoader knotUrlClassLoader = (URLClassLoader) getClass().getClassLoader().getParent();
		urls.addAll(Arrays.asList(knotUrlClassLoader.getURLs()));

		URLClassLoader dummyClassLoader = new URLClassLoader(urls.toArray(URL[]::new));
		return dummyClassLoader;
	}
}
