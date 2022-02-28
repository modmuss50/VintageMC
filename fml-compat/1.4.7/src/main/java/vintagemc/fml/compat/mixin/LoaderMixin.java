package vintagemc.fml.compat.mixin;

import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.relauncher.FMLRelauncher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vintagemc.fml.compat.FMLPreLaunch;

import java.util.Arrays;
import java.util.function.Function;

@Mixin(Loader.class)
public class LoaderMixin {

	@Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getClassLoader()Ljava/lang/ClassLoader;"))
	public ClassLoader redirectModClassLoader(Class instance) {
		// Use the dummy classloader we created in FMLRelauncherMixin
		return FMLRelauncher.instance().classLoader;
	}

	@Redirect(method = "identifyMods", at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", remap = false), remap = false)
	public Class<?> identifyMods(String ccl, boolean name, ClassLoader initialize) throws ClassNotFoundException {
		// Use the real classloader, not our hacky dummy "modClassLoader"
		return Class.forName(ccl, name, getClass().getClassLoader());
	}
}
