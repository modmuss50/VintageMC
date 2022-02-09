package vintagemc.fml.compat.mixin;

import cpw.mods.fml.relauncher.FMLInjectionData;

import vintagemc.fml.compat.FMLPreLaunch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FMLInjectionData.class)
public class FMLInjectionDataMixin {
	/**
	 * @author
	 */
	@Overwrite(remap = false)
	public static Object[] data() {
		// Return the custom injection data generated in prelaunch
		return FMLPreLaunch.injectionData;
	}
}
